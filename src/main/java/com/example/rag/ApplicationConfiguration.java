package com.example.rag;

import com.azure.search.documents.indexes.SearchIndexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.azure.AzureVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration

public class ApplicationConfiguration {

	private static final Logger methIDvectorStore = LoggerFactory.getLogger(ApplicationConfiguration.class.getName() + ".vectorStore()");
	private static final Logger methIDdumpConfig = LoggerFactory.getLogger(ApplicationConfiguration.class.getName() + ".dumpConfig()");

	@Value("${spring.ai.azure.openai.api-key}")
	private String OPENAI_API_KEY;

	@Value("${spring.ai.azure.openai.endpoint}")
	private String OPENAI_ENDPOINT;

	@Value("${spring.ai.azure.openai.chat.options.deployment-name}")
	private String OPENAI_DEPLOYMENT_NAME;

	@Value("${spring.ai.vectorstore.azure.api-key}")
	private String VECTORSTORE_API_KEY;

	@Value("${spring.ai.vectorstore.azure.url}")
	private String VECTORSTORE_URL;

	@Value("${spring.ai.vectorstore.azure.index-name}")
	private String VECTORSTORE_INDEX_NAME;

	@Bean
	public AzureVectorStore vectorStore(SearchIndexClient searchIndexClient, EmbeddingModel embeddingModel)
	{
		Logger logger = methIDvectorStore;
		logger.debug("Begins...");

		dumpConfig();

		var vectorStore = new AzureVectorStore(searchIndexClient, embeddingModel, true,
		List.of(AzureVectorStore.MetadataField.text("filename"),
				AzureVectorStore.MetadataField.int32("version")));
				vectorStore.setIndexName(VECTORSTORE_INDEX_NAME);

		logger.debug("Ends...");

		return vectorStore;
	}

	private void dumpConfig()
	{
		Logger logger = methIDdumpConfig;
		logger.debug("Begins...");

		logger.info("        OPENAI_API_KEY: " + "x-" + getLast4(OPENAI_API_KEY) );
		logger.info("       OPENAI_ENDPOINT: " + OPENAI_ENDPOINT );
		logger.info("OPENAI_DEPLOYMENT_NAME: " + OPENAI_DEPLOYMENT_NAME );

		logger.info("   VECTORSTORE_API_KEY: " + "x-" + getLast4(VECTORSTORE_API_KEY));
		logger.info("       VECTORSTORE_URL: " + VECTORSTORE_URL);
		logger.info("VECTORSTORE_INDEX_NAME: " + VECTORSTORE_INDEX_NAME);

		logger.debug("Ends...");

	}
	private String getLast4(String inputValue)
	{
		String returnValue = null;

		returnValue = inputValue == null || inputValue.length() < 4 ?
			inputValue : inputValue.substring(inputValue.length() -4);

		return( returnValue );
	}

}
