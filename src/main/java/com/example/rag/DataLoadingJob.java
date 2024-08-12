package com.example.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class DataLoadingJob implements CommandLineRunner {

	private static final Logger methIDload = LoggerFactory.getLogger(DataLoadingJob.class.getName() + ".load()");

	private ApplicationContext applicationContext;

	@Value("classpath:/data/AllSaints-en-US-v1.pdf")
	private Resource pdfResourceAllV1;

	@Value("classpath:/data/AllSaints-en-US-v2.pdf")
	private Resource pdfResourceAllV2;

	private final VectorStore vectorStore;

	@Autowired
	public DataLoadingJob(VectorStore vectorStore) {
		Assert.notNull(vectorStore, "VectorStore must not be null.");
		this.vectorStore = vectorStore;
	}

	@Override
	public void run(String... args) {
		load(pdfResourceAllV1, 1);
		load(pdfResourceAllV2, 2);
		System.exit(0);
	}
	public void load(Resource resource, int version)
	{
		Logger logger = methIDload;

		// Extract
		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
				PdfDocumentReaderConfig.builder()
					.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
						.withNumberOfBottomTextLinesToDelete(3)
						.withNumberOfTopPagesToSkipBeforeDelete(1)
						.build())
					.withPagesPerDocument(1)
					.build());

		// Transform
		var tokenTextSplitter = new TokenTextSplitter();

		logger.info("File {}.  Parsing splitting, creating embeddings and storing in vector store...", resource.getFilename());

		List<Document> splitDocuments = tokenTextSplitter.apply(pdfReader.get());
		// tag as external knowledge in the vector store's metadata
		for (Document splitDocument : splitDocuments) {
			splitDocument.getMetadata().put("filename", resource.getFilename());
			splitDocument.getMetadata().put("version", "" + version);
		}

		// Load
		this.vectorStore.accept(splitDocuments);

		logger.info("Done parsing document, splitting, creating embeddings and storing in vector store");

	}
}
