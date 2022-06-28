package com.jnj.auditlog.spring.repo;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.auditlog.spring.utlis.AlUtils;
import com.jnj.auditlog.spring.utlis.ContainerConstants;
import com.jnj.auditlog.spring.model.AlContainerData;
import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.model.CosmosProperties;
import com.microsoft.azure.documentdb.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Repository
public class AlCosmosDao {

    protected static Logger logger = LoggerFactory.getLogger(AlCosmosDao.class);

    private String collectionLink;

    private CosmosClient client;

    private DocumentClient documentClient;

    private CosmosContainer container;

    @Autowired
    private CosmosProperties cosmosProperties;

    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    private final CosmosAsyncClient cosmosAsyncClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public AlCosmosDao(CosmosAsyncClient cosmosAsyncClient) {
        this.cosmosAsyncClient = cosmosAsyncClient;
    }

    /**
     * Need to revisit this init method
     */
    @PostConstruct
    public void init() {
        log.info("Cosmos Repo .... init!!!");
        client = cosmosClientBuilder.buildClient();

        collectionLink = String.format("/dbs/%s/colls/%s", cosmosProperties.getDatabase(), "auditlog");

        documentClient = new DocumentClient(cosmosProperties.getUri(), cosmosProperties.getKey(), null, null);

        log.info("Cosmos Repo.....DocumentClient: "+documentClient);
    }

    // Container delete
    public Mono<CosmosContainerResponse> deleteAContainer() throws Exception {
        logger.info("Delete container " + cosmosProperties.getContainer() + " by ID.");
        return cosmosAsyncClient.getDatabase(cosmosProperties.getDatabase()).getContainer(cosmosProperties.getContainer()).delete();
    }

    public void saveAlItem(AlEventData alEventData) {
        this.cosmosAsyncClient.getDatabase(cosmosProperties.getDatabase())
                .getContainer(cosmosProperties.getContainer())
                .createItem(alEventData, new com.azure.cosmos.models.PartitionKey(alEventData.getProjectName()), new CosmosItemRequestOptions());
    }

    @SneakyThrows
    public List<AlEventData> getAlByAppNameAndPath(AlEventData alReqData) {

        List<com.microsoft.azure.documentdb.SqlParameter> paramList = new ArrayList<SqlParameter>();
        paramList.add(new com.microsoft.azure.documentdb.SqlParameter(ContainerConstants.PARTITION_KEY_PARAM, alReqData.getProjectName()));

        Map<String, Object> nestedJsonPathMap = objectMapper.readValue(objectMapper.writeValueAsString(alReqData), Map.class);

        StringBuilder nestedPathSql = new StringBuilder();

        nestedJsonPathMap.forEach((jsonStr, val) -> {
            if (ObjectUtils.isEmpty(val) && NumberUtils.isDigits(String.valueOf(val))) {
                nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+jsonStr, Integer.parseInt(String.valueOf(val))));
            } else if (val instanceof Map<?, ?>) {
                ((Map<?, ?>) val).forEach((pKey, pVal) -> {
                    if (!ObjectUtils.isEmpty(pVal)) {
                        nestedPathSql.append(" and c.payload."+pKey+" = @"+pKey);
                        if (NumberUtils.isDigits(String.valueOf(pVal))) {
                            paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+pKey, Integer.parseInt(String.valueOf(pVal))));
                        } else {
                            paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+pKey, pVal));
                        }
                    }
                });
            } else {
                if (!jsonStr.equalsIgnoreCase(ContainerConstants.PARTITION_KEY) && !ObjectUtils.isEmpty(val) && !jsonStr.equalsIgnoreCase("updateTimestamp")) {
                    nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                    paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+jsonStr, val));
                }
            }
        });

        SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE (c."+ContainerConstants.PARTITION_KEY+"= "+ContainerConstants.PARTITION_KEY_PARAM+nestedPathSql+")",
                new SqlParameterCollection(paramList));

        FeedOptions options = new FeedOptions();

        //for Non
        Iterator<Document> it = documentClient.queryDocuments(collectionLink, querySpec, options).getQueryIterator();
        List<AlEventData> alList = new LinkedList<>();
        while(it.hasNext()) {
            alList.add(AlUtils.documentToEntity(it.next()));
        }

        return alList;
    }

    @SneakyThrows
    public String prepareParamList_andQuery(final List<com.azure.cosmos.models.SqlParameter> cosmosSqlParamList, AlEventData alDataRequest) {
        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter(ContainerConstants.PARTITION_KEY_PARAM, alDataRequest.getProjectName()));

        Map<String, Object> nestedJsonPathMap = objectMapper.readValue(objectMapper.writeValueAsString(alDataRequest), Map.class);

        StringBuilder nestedPathSql = new StringBuilder();

        if (nestedJsonPathMap != null) {
            nestedJsonPathMap.forEach((jsonStr, val) -> {
                if (ObjectUtils.isEmpty(val) && NumberUtils.isDigits(String.valueOf(val))) {
                    nestedPathSql.append(" and c." + jsonStr + " = @" + jsonStr);
                    cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@" + jsonStr, Integer.parseInt(String.valueOf(val))));
                } else if (val instanceof Map<?, ?>) {
                    ((Map<?, ?>) val).forEach((pKey, pVal) -> {
                        if (!ObjectUtils.isEmpty(pVal)) {
                            nestedPathSql.append(" and c.payload." + pKey + " = @" + pKey);
                            if (NumberUtils.isDigits(String.valueOf(pVal))) {
                                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@" + pKey, Integer.parseInt(String.valueOf(pVal))));
                            } else {
                                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@" + pKey, pVal));
                            }
                        }
                    });
                } else {
                    if (!jsonStr.equalsIgnoreCase(ContainerConstants.PARTITION_KEY) && !ObjectUtils.isEmpty(val) && !jsonStr.equalsIgnoreCase("updateTimestamp")) {
                        nestedPathSql.append(" and c." + jsonStr + " = @" + jsonStr);
                        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@" + jsonStr, val));
                    }
                }
            });
        }

        String queryText = "SELECT * FROM c WHERE c." + ContainerConstants.PARTITION_KEY + " =  " + ContainerConstants.PARTITION_KEY_PARAM + nestedPathSql;
        if (ObjectUtils.isEmpty(alDataRequest.getProjectName())) {
            queryText = "SELECT * FROM c WHERE "+nestedPathSql;
        }
        return queryText;

    }

    public Flux<AlEventData> getAlStream_byNestedJson(AlEventData alReqData) {
        final List<com.azure.cosmos.models.SqlParameter> cosmosSqlParamList = new ArrayList<>();

        String queryText = prepareParamList_andQuery(cosmosSqlParamList, alReqData);

        Flux<AlEventData> returnFlux = this.cosmosAsyncClient
                .getDatabase(cosmosProperties.getDatabase()).getContainer(cosmosProperties.getContainer())
                .queryItems(new com.azure.cosmos.models.SqlQuerySpec(queryText, cosmosSqlParamList), AlContainerData.class)
//                .delayElements(Duration.ofSeconds(5))
                .map(AlUtils::containerToEntity).log();

        return returnFlux;

    }
}
