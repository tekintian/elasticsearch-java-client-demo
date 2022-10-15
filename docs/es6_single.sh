#!/bin/bash
# rm old container
docker rm -f elasticsearch6

# create new es container
docker run -itd --name elasticsearch6 -p 9200:9200 -p 9300:9300 \
    -v $PWD/es6.5_config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
    -v $PWD/es6.5_config/jvm.options:/usr/share/elasticsearch/config/jvm.options \
    -v $PWD/data:/usr/share/elasticsearch/data \
    elasticsearch:6.5.4

# check the es logs
# docker logs -f elasticsearch6
# check es health 
# http://127.0.0.1:9200/_cluster/health
