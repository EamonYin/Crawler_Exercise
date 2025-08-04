package com.crawler.crawler_exercise.service;

public interface IMilvusEmbeddingService {

    void insertMilvusInfo();
    void insertMilvusData(String data);
    String getMilvusInfo(String problem);

}
