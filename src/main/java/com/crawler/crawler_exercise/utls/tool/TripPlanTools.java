package com.crawler.crawler_exercise.utls.tool;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class TripPlanTools {

    // 高德地图配置
    private static final String AMAP_KEY = "";
    private static final String BASE = "https://restapi.amap.com";

    @Tool(description = "当用户需要规划两地路线，或是自驾路程，优化规划路线的时候调用这个工具")
    public String getTripPlan(@ToolParam(description = "规划路线的出发地") String departure,
                              @ToolParam(description = "规划路线的目的地") String destination,
                              @ToolParam(description = "规划路线的游玩时间") String tourTime) {
        log.info("🔧调用工具-[✈️getTripPlan] 出发地:{},目的地:{},游玩时间:{}", departure, destination, tourTime);
        try {
            // 1) 城市地理编码 -> 坐标
            CityInfo dep = geocodeCity(departure);
            CityInfo des = geocodeCity(destination);
            if (dep == null || des == null) {
                return "地理编码失败，请检查城市名称";
            }

            // 2) 驾车路线规划 -> 采样坐标
            List<String> samplePoints = drivingSamplePoints(dep.location, des.location);

            // 3) 逆地理编码 -> 提取沿途城市（含起终点）
            LinkedHashMap<String, CityInfo> routeCities = new LinkedHashMap<>(); // 按顺序去重
            CityInfo depCity = reverseGeocode(dep.location);
            if (depCity != null && depCity.citycode != null) routeCities.put(depCity.citycode, depCity);
            for (String p : samplePoints) {
                CityInfo c = reverseGeocode(p);
                if (c != null && c.citycode != null && !routeCities.containsKey(c.citycode)) {
                    routeCities.put(c.citycode, c);
                }
            }
            CityInfo desCity = reverseGeocode(des.location);
            if (desCity != null && desCity.citycode != null && !routeCities.containsKey(desCity.citycode)) routeCities.put(desCity.citycode, desCity);

            // 4) 查询每个城市的酒店与景点（各取前5个）
            JSONArray citiesArr = new JSONArray();
            for (CityInfo c : routeCities.values()) {
                Thread.sleep(1002);
                List<PlaceInfo> hotels = searchPlaces(c.citycode, "酒店", 5);
                List<PlaceInfo> spots = searchPlaces(c.citycode, "景点", 5);
                JSONObject cjo = new JSONObject();
                cjo.put("city", c.city);
                cjo.put("citycode", c.citycode);
                cjo.put("adcode", c.adcode);
                // todo：这里需要把酒店的 name、 address、location、rating、photos、description、bookingUrl全部记录下来，而不是只记录name
                cjo.put("hotels", toSimpleArray(hotels));
                // todo：这里需要把景点的 name、 address、location、rating、photos、description、bookingUrl全部记录下来，而不是只记录name
                cjo.put("spots", toSimpleArray(spots));
                citiesArr.add(cjo);
            }

            // 5) 组织返回
            JSONObject out = new JSONObject();
            out.put("departure", departure);
            out.put("destination", destination);
            out.put("tourTime", tourTime);
            out.put("routeCities", citiesArr);
            return out.toJSONString();
        } catch (Exception e) {
            log.warn("getTripPlan 执行异常", e);
            return "路线规划失败:" + e.getMessage();
        }
    }

    // —— 以下为辅助方法 ——

    // 城市地理编码（城市名 -> 坐标/编码）
    private CityInfo geocodeCity(String cityName) throws Exception {
        String url = BASE + "/v3/geocode/geo?address=" + enc(cityName) + "&key=" + AMAP_KEY;
        String body = doGet(url);
        JSONObject jo = JSONObject.parseObject(body);
        if (!"1".equals(jo.getString("status"))) return null;
        JSONArray geos = jo.getJSONArray("geocodes");
        if (geos == null || geos.isEmpty()) return null;
        JSONObject g0 = geos.getJSONObject(0);
        CityInfo c = new CityInfo();
        c.location = g0.getString("location"); // lng,lat
        c.citycode = g0.getString("citycode");
        c.adcode = g0.getString("adcode");
        String city = g0.getString("city");
        if (city == null || city.isEmpty() || "[]".equals(city)) city = g0.getString("province");
        c.city = city;
        return c;
    }

    // 驾车路径规划，按每一步的中点采样一个坐标
    private List<String> drivingSamplePoints(String origin, String destination) throws Exception {
        String url = BASE + "/v5/direction/driving?key=" + AMAP_KEY + "&origin=" + enc(origin) + "&destination=" + enc(destination) + "&show_fields=polyline";
        String body = doGet(url);
        JSONObject jo = JSONObject.parseObject(body);
        JSONArray steps = jo.getJSONObject("route") != null && jo.getJSONObject("route").getJSONArray("paths") != null
                ? jo.getJSONObject("route").getJSONArray("paths").getJSONObject(0).getJSONArray("steps")
                : null;
        List<String> samples = new ArrayList<>();
        if (steps == null) return samples;
        for (int i = 0; i < steps.size(); i++) {
            JSONObject step = steps.getJSONObject(i);
            String polyline = step.getString("polyline");
            if (polyline == null || polyline.isEmpty()) continue;
            String[] pts = polyline.split(";");
            samples.add(pts[pts.length / 2]); // 取中点
        }
        return samples;
    }

    // 逆地理编码（坐标 -> 城市信息）
    private CityInfo reverseGeocode(String lnglat) throws Exception {
        String url = BASE + "/v3/geocode/regeo?key=" + AMAP_KEY + "&location=" + enc(lnglat);
        String body = doGet(url);
        JSONObject jo = JSONObject.parseObject(body);
        if (!"1".equals(jo.getString("status"))) return null;
        JSONObject comp = jo.getJSONObject("regeocode").getJSONObject("addressComponent");
        if (comp == null) return null;
        CityInfo c = new CityInfo();
        String city = comp.getString("city");
        if (city == null || city.isEmpty() || "[]".equals(city)) city = comp.getString("province");
        c.city = city;
        c.citycode = comp.getString("citycode");
        c.adcode = comp.getString("adcode");
        c.location = lnglat;
        return c;
    }

    // 地点文本检索（酒店/景点）
    private List<PlaceInfo> searchPlaces(String citycode, String keywords, int size) throws Exception {
        String url = BASE + "/v5/place/text?key=" + AMAP_KEY
                + "&city=" + enc(citycode)
                + "&keywords=" + enc(keywords)
                + "&page_size=" + size + "&page_num=1"
                + "&show_fields=photos,rating,tag,business";
        String body = doGet(url);
        JSONObject jo = JSONObject.parseObject(body);
        JSONArray pois = jo.getJSONArray("pois");
        List<PlaceInfo> list = new ArrayList<>();
        if (pois == null) return list;
        for (int i = 0; i < Math.min(pois.size(), size); i++) {
            JSONObject p = pois.getJSONObject(i);
            PlaceInfo pi = new PlaceInfo();
            pi.id = p.getString("id");
            pi.name = p.getString("name");
            pi.address = p.getString("address");
            pi.location = p.getString("location");
            pi.rating = p.getString("rating");
            // 处理图片
            JSONArray phs = p.getJSONArray("photos");
            if (phs != null && !phs.isEmpty()) {
                List<String> urls = new ArrayList<>();
                for (int j = 0; j < phs.size(); j++) {
                    JSONObject ph = phs.getJSONObject(j);
                    String u = ph == null ? null : ph.getString("url");
                    if (u != null && !u.isEmpty()) {
                        urls.add(u);
                    }
                }
                if (!urls.isEmpty()) {
                    pi.photos = urls;
                }
            }
            // 描述：优先使用 tag，其次业务区，最后回退地址
            String desc = p.getString("tag");
            if (desc == null || desc.isEmpty()) {
                JSONObject biz = p.getJSONObject("business");
                if (biz != null) {
                    String ba = biz.getString("business_area");
                    if (ba != null && !ba.isEmpty()) desc = ba;
                }
            }
            if (desc == null || desc.isEmpty()) {
                desc = p.getString("address");
            }
            pi.description = desc;
            // 预订链接：构造高德移动端详情页链接
            if (pi.id != null && !pi.id.isEmpty()) {
                pi.bookingUrl = "https://www.amap.com/place/" + pi.id;
            }
            list.add(pi);
        }
        return list;
    }

    private JSONArray toSimpleArray(List<PlaceInfo> places) {
        JSONArray arr = new JSONArray();
        if (places == null) return arr;
        for (PlaceInfo p : places) {
            if (p == null) continue;
            JSONObject o = new JSONObject();
            o.put("name", p.name);
            o.put("address", p.address);
            o.put("location", p.location);
            o.put("rating", p.rating);
            // photos 列表
            if (p.photos != null) {
                JSONArray photosArr = new JSONArray();
                photosArr.addAll(p.photos);
                o.put("photos", photosArr);
            } else {
                o.put("photos", new JSONArray());
            }
            o.put("description", p.description);
            o.put("bookingUrl", p.bookingUrl);
            arr.add(o);
        }
        return arr;
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private String doGet(String url) throws Exception {
        // 统一的GET请求封装
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse resp = client.execute(get)) {
                String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                log.debug("AMap响应: {}", body);
                return body;
            }
        }
    }

    // 简单数据模型
    private static class CityInfo {
        String city;      // 城市名
        String citycode;  // 城市码
        String adcode;    // 区域码
        String location;  // 坐标 lng,lat
    }

    private static class PlaceInfo {
        String id;
        String name;
        String address;
        String location;
        String rating;
        List<String> photos;
        String description;
        String bookingUrl;
    }
}
