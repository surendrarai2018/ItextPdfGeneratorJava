package com.diaspark;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.jsoup.Jsoup;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class PDFGeneratorUtill implements CommonConstant {
	private Properties prop = null;

	public static void main(String args[]) throws Exception {
		new PDFGeneratorUtill().createPdf();
	}

	public PDFGeneratorUtill() {
		InputStream is = null;
		try {
			this.prop = new Properties();
			is = this.getClass().getResourceAsStream("/conf.properties");
			prop.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPropertyValue(String key) {
		return this.prop.getProperty(key);
	}

	private void createPdf() throws Exception {
		PDFGeneratorUtill mpc = new PDFGeneratorUtill();
		File cssTestFile = new File(
				this.getClass().getClassLoader().getResource(mpc.getPropertyValue(CSS_FILE_PATH)).getFile());

		CSSResolver cssResolver = new StyleAttrCSSResolver();
		CssFile cssFile = XMLWorkerHelper.getCSS(new ByteArrayInputStream(Files.readAllBytes(cssTestFile.toPath())));
		cssResolver.addCss(cssFile);
		// HTML
		HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
		// Pipelines
		ElementList elements = new ElementList();
		ElementHandlerPipeline pdf = new ElementHandlerPipeline(elements, null);
		HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
		CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);
		// XML Worker
		XMLWorker worker = new XMLWorker(css, true);
		XMLParser p = new XMLParser(worker);
		p.parse(new ByteArrayInputStream(updateDynamicPlaceHolders(
				this.getClass().getClassLoader().getResource(mpc.getPropertyValue(HTML_SOURCE_PATH)).getFile(),
				getEnrollmentFormList()).getBytes()));

		String dest = mpc.getPropertyValue(PDF_DESTINATION_PATH);
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(dest));
		document.open();
		PdfPTable pdfPTable = (PdfPTable) elements.get(0);
		document.add(pdfPTable);
		document.close();
	}

	public static EnrollmentFormDto getEnrollmentFormList() {
		EnrollmentFormDto formDto = new EnrollmentFormDto();
		formDto.setGrades(GRADE_VALUE);
		formDto.setIndicator(INDICATOR_VALUE);
		formDto.setLimitOrEnrolled(ENROLLED_LIMIT_VALUE);
		formDto.setLocation(LOCATION_VALUE);
		formDto.setPrograms(PROGRAMS_VALUE);
		formDto.setProvider(PROVIDER_VALUE);
		formDto.setSchoolYear(SCHOOL_YEAR_VALUE);
		formDto.setType(TYPE_VALUE);
		formDto.setAge(AGE_VALUE);
		formDto.setProgramStartDate(PROGRAM_START_DATE_VALUE);
		formDto.setProgramEndDate(PROGRAM_END_DATE_VALUE);
		return formDto;
	}

	private static String updateDynamicPlaceHolders(String htmlpath, EnrollmentFormDto dto) {
		StringBuilder buildTmpHTML = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(htmlpath));

			String line;
			while ((line = br.readLine()) != null) {
				buildTmpHTML.append(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		org.jsoup.nodes.Document doc = Jsoup.parse(buildTmpHTML.toString());
		doc.getElementById("INDICATOR_KEY").text(INDICATOR_KEY);
		doc.getElementById("INDICATOR_VALUE").text(dto.getIndicator());
		return doc.toString();
	}

}