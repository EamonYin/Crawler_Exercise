package com.crawler.crawler_exercise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crawler.crawler_exercise.config.EamonGPTConfig;
import com.crawler.crawler_exercise.entiy.QwenMsg;
import com.crawler.crawler_exercise.mapper.QwenMsgMapper;
import com.crawler.crawler_exercise.service.IQwenMsgService;
import com.crawler.crawler_exercise.utils.api.QwenAPi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class QwenMsgServiceImpl extends ServiceImpl<QwenMsgMapper, QwenMsg> implements IQwenMsgService {

    @Resource
    EamonGPTConfig eamonGPTConfig;
    @Autowired
    QwenMsgMapper qwenMsgMapper;

    @Override
    public String sendSmsCN(String phoneNum) {
        QwenAPi qwenAPi = new QwenAPi();
        try {
            String phoneCode = "86";
            String countryCode = "CN";
            // 返回 smstoken
            String smstoken = qwenAPi.sendSms(phoneCode, phoneNum, countryCode);
            if(StringUtils.isNoneBlank(smstoken)){
                // 删除所有存在的数据
                UpdateWrapper<QwenMsg> qwenMsgUpdateWrapper = new UpdateWrapper<>();
                qwenMsgUpdateWrapper.lambda().eq(QwenMsg::getDeFlg,0);
                qwenMsgUpdateWrapper.set("de_flg",1);
                this.update(qwenMsgUpdateWrapper);
                // 插入新的数据
                QwenMsg qwenMsg = new QwenMsg();
                qwenMsg.setPhoneCode(phoneCode);
                qwenMsg.setLoginId(phoneNum);
                qwenMsg.setCountryCode(countryCode);
                qwenMsg.setSmsToken(smstoken);
                qwenMsg.setDeFlg(0);
                qwenMsgMapper.insert(qwenMsg);
                return smstoken;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "获取次数已达上限，请24小时后再试";
    }

    @Override
    public String login(String phoneNum, String smsCode) {
        QwenAPi qwenAPi = new QwenAPi();
        try {
            LambdaQueryWrapper<QwenMsg> qwenMsgLambdaQueryWrapper = new LambdaQueryWrapper<>();
            qwenMsgLambdaQueryWrapper.eq(QwenMsg::getDeFlg, 0);
            qwenMsgLambdaQueryWrapper.eq(QwenMsg::getLoginId, phoneNum);
            QwenMsg qwenMsg = qwenMsgMapper.selectOne(qwenMsgLambdaQueryWrapper);
            // 获取票据
            String tongyiSsoTicket = qwenAPi.login(qwenMsg.getPhoneCode(), phoneNum, qwenMsg.getCountryCode(), qwenMsg.getSmsToken(), smsCode);
            qwenMsg.setTongyiSsoTicket(tongyiSsoTicket);
            qwenMsgMapper.updateById(qwenMsg);
            eamonGPTConfig.upDateEamonGPTKey(tongyiSsoTicket);
            return tongyiSsoTicket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
