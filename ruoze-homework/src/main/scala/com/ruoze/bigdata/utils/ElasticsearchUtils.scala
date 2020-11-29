package com.ruoze.bigdata.utils

import java.util

import org.apache.http.HttpHost
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.asyncsearch.SubmitAsyncSearchRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.{SearchHit, SearchHits}
import org.elasticsearch.search.builder.SearchSourceBuilder

/**
 * 参考官网：https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.9/java-rest-high.html
 */
object ElasticsearchUtils {


  def getHighLevelClient(): RestHighLevelClient = {
    val restHighLevelClient: RestHighLevelClient = new RestHighLevelClient(
      RestClient.builder(
        new HttpHost("hadoop01", 9200, "http")
      ))
    restHighLevelClient
  }


  def close(restHighLevelClient: RestHighLevelClient): Unit = {
    if (null != restHighLevelClient) {
      restHighLevelClient.close
    }
  }


  def query2(restHighLevelClient: RestHighLevelClient, input: String): Array[String] = {

    val searchSource = new SearchSourceBuilder().query(QueryBuilders.termQuery("domain", input))
    val indices = Array[String]("access_log_df")
    val request = new SubmitAsyncSearchRequest(searchSource, indices: _*)
    request.setWaitForCompletionTimeout(TimeValue.timeValueSeconds(30));
    request.setKeepAlive(TimeValue.timeValueMinutes(15));
    request.setKeepOnCompletion(false);

    val response = restHighLevelClient.asyncSearch()
      .submit(request, RequestOptions.DEFAULT)

    val hits: Array[SearchHit] = response.getSearchResponse.getHits.getHits
    val urls = for (hit <- hits) yield {
      hit.getSourceAsMap.get("url").toString
    }
    urls
  }


  def query(restHighLevelClient: RestHighLevelClient, domain: String): Array[String] = {
    val searchRequest: SearchRequest = new SearchRequest
    searchRequest.indices("access_log_df")
    val searchSourceBuilder: SearchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.termQuery("domain", domain))
    searchRequest.source(searchSourceBuilder)
    val response: SearchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    val hits: Array[SearchHit] = response.getHits.getHits

    val urls = for (elem <- hits) yield {
      val map: util.Map[String, AnyRef] = elem.getSourceAsMap
      map.get("url").toString
    }
    urls
  }

  def main(args: Array[String]): Unit = {
    val client: RestHighLevelClient = getHighLevelClient()

    println(client)
    val url = query(client, "ruozedata.com")
    println(url)
    close(client)
  }


}
