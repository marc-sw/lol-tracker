package com.fumano.crawler.mapper;

public interface Mapping <From, To>{
    To map(From object);
}
