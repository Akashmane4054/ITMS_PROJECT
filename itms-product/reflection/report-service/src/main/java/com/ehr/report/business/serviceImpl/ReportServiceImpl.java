package com.ehr.report.business.serviceImpl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.AssessmentUtil;
import com.ehr.core.util.Constants;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.UserUtil;
import com.ehr.report.business.service.ReportService;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

	private static final String CLASSNAME = ReportServiceImpl.class.getSimpleName();

	@Autowired
	private AssessmentUtil assessmentUtil;

	@Autowired
	private UserUtil userUtil;

	@Override
	public Map<String, Object> getAssessmentIndividualInsight(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, Object> verifyToken(MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	@Override
	public Map<String, Object> generateAssessmentCorporateInsight(Long assessmentId,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();

		try {

			Map<String, Object> userResponse = verifyToken(headers);

			byte[] pdfBytes = createAssessmentPdf(assessmentId, headers);
			String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

			response.put("message", "PDF generated successfully");
			response.put("pdfData", base64Pdf);
			response.put("fileName", "assessment_" + assessmentId + ".pdf");

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	public byte[] createAssessmentPdf(Long assessmentId, MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException, IOException {

		Map<String, Object> response = assessmentUtil.getParticipationByStatusAndAge(assessmentId, headers);

		Map<String, Object> sections = assessmentUtil.getTemplateSectionsByAssessmentId(assessmentId, headers);

		Long physicalHealthId = null;
		Long mentalHealthId = null;
		Long sleepStatusId = null;
		Long digitalHealthId = null;

		if (sections.containsKey("sections")) {
			List<Map<String, Object>> sectionList = (List<Map<String, Object>>) sections.get("sections");
			for (Map<String, Object> section : sectionList) {
				String name = (String) section.get("name");
				Long id = ((Number) section.get("id")).longValue();
				if ("Physical Health".equalsIgnoreCase(name)) {
					physicalHealthId = id;
				} else if ("Mental Health".equalsIgnoreCase(name)) {
					mentalHealthId = id;
				} else if ("Sleep".equalsIgnoreCase(name)) {
					sleepStatusId = id;
				} else if ("Digital Health".equalsIgnoreCase(name)) {
					digitalHealthId = id;
				}
			}
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDocument = new PdfDocument(writer);
		Document document = new Document(pdfDocument, PageSize.A4);
		document.setMargins(40, 40, 40, 40);

		// Add title
		addTitle(document);
		document.add(new AreaBreak()); // Start a new page

		// Add Highlights
		addHighlights(document);
		document.add(new AreaBreak()); // Start a new page

		// Add Findings and Table
		addFindingsAndTable(document, response);

		addGenderSummaryTable(document, response);

		addBarChart(document, response);
		document.add(new AreaBreak()); // Start a new page

		addParticipationChart(document, response);
		document.add(new AreaBreak()); // Start a new page

		addHealthStatusTable(document, assessmentId, physicalHealthId, headers);

		HealthStatusChart(document, assessmentId, physicalHealthId, headers);

		document.add(new AreaBreak()); // Start a new page

		GeneralCorpateHealthStatus(document, assessmentId, physicalHealthId, headers);

//        addNutritionStatus(document);

		document.add(new AreaBreak()); // Start a new page

		addMentalHealthStatusTable(document, assessmentId, mentalHealthId, headers);

		MentalHealthStatusChart(document, assessmentId, mentalHealthId, headers);

		MentalHealthStatus(document, assessmentId, mentalHealthId, headers);

		document.add(new AreaBreak());

		addDigitalHealthStatusTable(document, assessmentId, digitalHealthId, headers);
		DigitalHealthStatusChart(document, assessmentId, digitalHealthId, headers);

		document.add(new AreaBreak());

		addSleepStatusTable(document, assessmentId, sleepStatusId, headers);
		SleepStatusChart(document, assessmentId, sleepStatusId, headers);

		document.add(new AreaBreak());

		// Add Conclusion
		addConclusion(document);
		document.close();

		log.info("PDF generated successfully for assessmentId: {}", assessmentId);
		return outputStream.toByteArray();
	}

	private void addTitle(Document document) {
		DeviceRgb navyBlue = new DeviceRgb(0, 51, 102); // Darker blue shade

		document.add(new Paragraph("HEALTH QUARTERS \n").setFontColor(navyBlue).setBold().setFontSize(18)
				.setTextAlignment(TextAlignment.CENTER));

		document.add(
				new Paragraph("Healthquarters India Pvt. Ltd.\nAnalysis of\nHealth Risk Assessment of employees of")
						.setFontColor(navyBlue).setBold().setFontSize(15).setTextAlignment(TextAlignment.CENTER));

		document.add(new Paragraph("\n"));
	}

	private void addHighlights(Document document) {
		DeviceRgb blueColor = new DeviceRgb(0, 0, 255);

		document.add(new Paragraph("Key Highlights of the HRA program").setFontColor(blueColor).setBold().setUnderline()
				.setFontSize(14).setTextAlignment(TextAlignment.CENTER));

		document.add(new Paragraph(
				"We would like to thank all the stakeholders at XXX for providing Healthquarters India Pvt. Ltd. an opportunity to "
						+ "design and execute the Health Risk Assessment (HRA) for their employees. We developed and deployed the program "
						+ "from scratch, completely customized to suit the requirements of the HR division of XXX.\n\n"
						+ "It was HQ’s first time at XXX conducting an HRA program with a different approach. Keeping in mind the vast employee "
						+ "strength to be covered, constraint of time, and based on XXX’s suggestion, HQ adopted an online approach.\n\n"
						+ "Around 2530 employees participated in this program during the term of 31 days (20 March till 26 April 2019), with an "
						+ "average of 82 employees per day.\n\n"
						+ "This document contains the details of the observations & recommendations from our expert panel for your easy reference."));

		document.add(new Paragraph(
				"Key Recommendations - XXX should consider introducing the following programs for their employees:")
				.setBold().setFontSize(14));

		document.add(new Paragraph("1. Onsite Clinic\n" + "2. Diet and nutrition sessions\n"
				+ "3. Psychiatric / psychotherapist sessions\n" + "4. Life coach for work-life balance\n"
				+ "5. Detailed health check-ups for the employees"));
	}

	private void addFindingsAndTable(Document document, Map<String, Object> response) {
		DeviceRgb blueColor = new DeviceRgb(0, 0, 255);
		document.add(new Paragraph("Analysis of the findings").setFontColor(blueColor).setBold().setUnderline()
				.setFontSize(16).setTextAlignment(TextAlignment.CENTER));

		document.add(new Paragraph("Status & Age wise participation:").setFontColor(blueColor).setUnderline()
				.setFontSize(16).setTextAlignment(TextAlignment.LEFT));

		float[] columnWidths = { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
		Table table = new Table(columnWidths);
		table.setWidth(500);

		table.addCell(new Cell(2, 1).add(new Paragraph("Age Group")).setBold());
		table.addCell(new Cell(1, 5).add(new Paragraph("Female")).setBold().setTextAlignment(TextAlignment.CENTER));
		table.addCell(new Cell(1, 5).add(new Paragraph("Male")).setBold().setTextAlignment(TextAlignment.CENTER));

		String[] headers = { "18-20", "21-30", "31-40", "41-50", "51-56", "18-20", "21-30", "31-40", "41-50", "51-56" };
		for (String header : headers) {
			table.addCell(new Cell().add(new Paragraph(header)).setBold().setTextAlignment(TextAlignment.CENTER));
		}

		Map<String, Object> generalAverage = (Map<String, Object>) response.get("generalAverage");

		String[] statusCategories = { "Complete", "Incomplete", "GrandTotal" };

		String[] genders = { "Female", "Male" };

		for (String status : statusCategories) {
			List<String> rowData = new ArrayList<>();
			rowData.add(status);

			for (String gender : genders) {
				Map<String, Object> genderData = (Map<String, Object>) generalAverage.get(gender);
				Map<String, Integer> statusData = (Map<String, Integer>) genderData.get(status);

				for (String ageGroup : Arrays.asList("18-20", "21-30", "31-40", "41-50", "51-56")) {
					rowData.add(String.valueOf(statusData.getOrDefault(ageGroup, 0))); // Add age-wise data
				}
			}

			for (String cellData : rowData) {
				table.addCell(new Cell().add(new Paragraph(cellData)).setTextAlignment(TextAlignment.CENTER));
			}
		}

		document.add(table);
	}

	private static void addGenderSummaryTable(Document document, Map<String, Object> response) {
		document.add(new Paragraph("\n").setMarginBottom(20));

		Table summaryTable = new Table(new float[] { 4, 4, 4 });
		summaryTable.setWidth(500);

		summaryTable.addHeaderCell(new Cell().add(new Paragraph("Gender")).setTextAlignment(TextAlignment.CENTER));
		summaryTable
				.addHeaderCell(new Cell().add(new Paragraph("No. of Members")).setTextAlignment(TextAlignment.CENTER));
		summaryTable.addHeaderCell(new Cell().add(new Paragraph("Percentile")).setTextAlignment(TextAlignment.CENTER));

		Map<String, Object> genderAverage = (Map<String, Object>) response.get("genderAverage");

		String[] categories = { "male", "female", "total" };
		String[] labels = { "Male", "Female", "TOTAL" };

		for (int i = 0; i < categories.length; i++) {
			Map<String, Object> genderData = (Map<String, Object>) genderAverage.get(categories[i]);
			int noOfMembers = (int) genderData.get("noOfMembers");
			double percentage = (double) genderData.get("percentage");

			summaryTable.addCell(new Cell().add(new Paragraph(labels[i])).setTextAlignment(TextAlignment.CENTER));
			summaryTable.addCell(
					new Cell().add(new Paragraph(String.valueOf(noOfMembers))).setTextAlignment(TextAlignment.CENTER));
			summaryTable.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", percentage)))
					.setTextAlignment(TextAlignment.CENTER));
		}

		document.add(summaryTable);
		document.add(new Paragraph("\n").setMarginBottom(20));
	}

	private static void addBarChart(Document document, Map<String, Object> response) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] ageGroups = { "18-20", "21-30", "31-40", "41-50", "51-56" };
		String[] completionStatus = { "Complete", "Incomplete", "GrandTotal" };

		// Using short gender abbreviations
		Map<String, String> genderAbbreviation = new HashMap<>();
		genderAbbreviation.put("Male", "M");
		genderAbbreviation.put("Female", "F");

		Map<String, Object> generalAverage = (Map<String, Object>) response.get("generalAverage");

		for (String gender : genderAbbreviation.keySet()) {
			Map<String, Object> genderData = (Map<String, Object>) generalAverage.get(gender);
			for (String status : completionStatus) {
				Map<String, Object> statusData = (Map<String, Object>) genderData.get(status);
				for (String ageGroup : ageGroups) {
					int value = (int) statusData.getOrDefault(ageGroup, 0);

					// Updated X-axis label format
					String label = genderAbbreviation.get(gender) + " - " + ageGroup;
					dataset.addValue(value, status, label);
				}
			}
		}

		JFreeChart barChart = ChartFactory.createBarChart("Age group & Gender wise participation", "Age Group & Gender", // X-axis
				"Count", // Y-axis
				dataset, PlotOrientation.VERTICAL, true, true, false);

		CategoryPlot plot = barChart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		Color[] colors = { new Color(52, 152, 219), new Color(46, 204, 113), new Color(155, 89, 182) };

		for (int i = 0; i < completionStatus.length; i++) {
			renderer.setSeriesPaint(i, colors[i]);
		}

		renderer.setMaximumBarWidth(0.1);

		// Set plot background and gridlines
		plot.setBackgroundPaint(new Color(240, 240, 240)); // Light gray background
		plot.setDomainGridlinePaint(Color.DARK_GRAY); // Dark grid lines
		plot.setRangeGridlinePaint(Color.GRAY);

		// Adjust X-axis label position to prevent truncation
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotates labels 45 degrees
		domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Reduce font size for clarity

		// Set fonts for title and axis labels
		Font titleFont = new Font("Arial", Font.BOLD, 16);
		Font axisFont = new Font("Arial", Font.PLAIN, 12);

		barChart.getTitle().setFont(titleFont);
		plot.getDomainAxis().setLabelFont(axisFont);
		plot.getRangeAxis().setLabelFont(axisFont);

		// Improve legend readability
		barChart.getLegend().setPosition(RectangleEdge.BOTTOM);
		barChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));

		try {
			BufferedImage chartImage = barChart.createBufferedImage(700, 400);
			ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", chartOutputStream);
			Image chartPdfImage = new Image(ImageDataFactory.create(chartOutputStream.toByteArray()));
			document.add(chartPdfImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addParticipationChart(Document document, Map<String, Object> response) throws IOException {
		try {
			// Data extraction remains the same
			Map<String, Object> genderAverage = (Map<String, Object>) response.get("genderAverage");
			if (genderAverage == null) {
				throw new IllegalArgumentException("genderAverage data not found in response");
			}

			Map<String, Object> maleData = (Map<String, Object>) genderAverage.get("male");
			Map<String, Object> femaleData = (Map<String, Object>) genderAverage.get("female");
			Map<String, Object> totalData = (Map<String, Object>) genderAverage.get("total");

			int maleCount = ((Number) maleData.get("noOfMembers")).intValue();
			int femaleCount = ((Number) femaleData.get("noOfMembers")).intValue();
			int total = ((Number) totalData.get("noOfMembers")).intValue();
			double malePercentage = ((Number) maleData.get("percentage")).doubleValue();
			double femalePercentage = ((Number) femaleData.get("percentage")).doubleValue();

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			dataset.addValue(maleCount, "Participation", "Male");
			dataset.addValue(femaleCount, "Participation", "Female");

			JFreeChart chart = ChartFactory.createBarChart(null, "", "", dataset, PlotOrientation.VERTICAL, false,
					false, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.WHITE);
			plot.setOutlineVisible(false);
			plot.setRangeGridlinePaint(new Color(220, 220, 220));

			BarRenderer renderer = (BarRenderer) plot.getRenderer();
			renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Male color
			renderer.setSeriesPaint(1, new Color(0, 176, 240)); // Female color
			renderer.setShadowVisible(false);
			renderer.setBarPainter(new StandardBarPainter());
			renderer.setItemMargin(0.2);
			renderer.setMaximumBarWidth(0.1);

			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryMargin(0.4);
			domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
			domainAxis.setTickLabelPaint(Color.BLACK);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			rangeAxis.setRange(0, Math.max(10, total * 1.2));
			rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 8));
			rangeAxis.setTickMarkPaint(Color.GRAY);

			BufferedImage chartImage = chart.createBufferedImage(400, 250);

			BufferedImage combinedImage = new BufferedImage(500, 350, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = combinedImage.createGraphics();

			try {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, 500, 350);

				g.drawImage(chartImage, 40, 50, null);

				g.setColor(Color.BLACK);
				g.setFont(new Font("Arial", Font.BOLD, 14));
				g.drawString("Participation", 200, 20);

				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(String.format("%.0f%%", malePercentage), 120, 45);
				g.drawString(String.format("%.0f%%", femalePercentage), 280, 45);

				g.setFont(new Font("Arial", Font.PLAIN, 10));
				g.drawString(String.valueOf(maleCount), 120, 330);
				g.drawString(String.valueOf(femaleCount), 280, 330);

				g.setFont(new Font("Arial", Font.BOLD, 10));
				g.drawString("Total: " + total, 220, 345);
			} finally {
				g.dispose();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(combinedImage, "png", baos);
			ImageData imageData = ImageDataFactory.create(baos.toByteArray());
			document.add(new Image(imageData).setAutoScale(true));
			document.add(new Paragraph("\n"));

			document.add(new Paragraph("Key points:").setBold().setFontSize(14));

			document.add(
					new Paragraph("1. Highest no of participants are for the age group: 21-40 years – 1948 members\n"
							+ "2. Out of 31 days of the program duration 3 days were non productive due to outage\n"
							+ "3. 7 times no. of participation jumped over 100 per day\n"
							+ "4. Maximum participants on any given day was 393 on 3 rd April\n"
							+ "5. Lowest participation was observed on 7 th April – only 2 members attempted and completed the HRA on 7 th April"));

		} catch (Exception e) {
			document.add(new Paragraph("Error generating chart: " + e.getMessage()));
		}
	}

	private void addHealthStatusTable(Document document, Long assessmentId, Long physicalHealthId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		document.add(new Paragraph("General Corporate Health Status").setBold().setFontSize(14)
				.setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

		float[] columnWidths = { 3, 2, 2, 2, 2, 2 };
		Table table = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));

		// Header rows
		table.addCell(createHeaderCell("Gender / Age Band", 1, 2));
		table.addCell(createHeaderCell("Low", 2, 1, new DeviceRgb(160, 212, 104))); // Green
		table.addCell(createHeaderCell("Moderate", 1, 1, new DeviceRgb(255, 206, 84))); // Yellow
		table.addCell(createHeaderCell("High", 1, 1, new DeviceRgb(237, 85, 101))); // Red
		table.addCell(createHeaderCell("Total", 1, 2));

		// Subheaders
		table.addCell(createSubHeaderCell("Outstanding!"));
		table.addCell(createSubHeaderCell("You're doing well"));
		table.addCell(createSubHeaderCell("Need to Improve a lot"));
		table.addCell(createSubHeaderCell("Need Immediate intervention"));

		addDynamicHealthStatusRows(table, assessmentId, physicalHealthId, headers);

		document.add(table);
	}

	private void addDynamicHealthStatusRows(Table table, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, sectionId,
				headers);

		Map<String, Long> overallCounts = getSafeLongMap(sectionRecord.get("overAllCounts"));
		Map<String, Map<String, Long>> percentages = getSafeNestedLongMap(sectionRecord.get("Percentages"));
		Map<String, Map<String, Object>> genderAgeBand = getSafeNestedObjectMap(sectionRecord.get("GenderAgeBand"));

		Long femaleTotal = ((Number) sectionRecord.getOrDefault("Female Total", 0)).longValue();
		Long maleTotal = ((Number) sectionRecord.getOrDefault("Male Total", 0)).longValue();
		Long overallTotal = ((Number) sectionRecord.getOrDefault("Overall Total", 0)).longValue();

		for (Map.Entry<String, Map<String, Object>> entry : genderAgeBand.entrySet()) {
			String gender = entry.getKey();
			Map<String, Object> genderData = entry.getValue();

			addGenderTotalRow(table, gender, genderData, maleTotal, femaleTotal);

			addAgeBandRows(table, genderData);
		}

		addOverallTotalRow(table, overallCounts, overallTotal);

		addPercentageRows(table, percentages);
	}

	private void addGenderTotalRow(Table table, String gender, Map<String, Object> genderData, Long maleTotal,
			Long femaleTotal) {

		table.addCell(createBoldCell(gender + " Total"));

		for (String category : getHealthCategories()) {
			Long count = extractGenderTotalValue(genderData, category, gender);
			table.addCell(createDataCell(String.valueOf(count)));
		}

		Long genderTotal = gender.equalsIgnoreCase("Male") ? maleTotal : femaleTotal;
		table.addCell(createBoldCell(String.valueOf(genderTotal)));
	}

	private Long extractGenderTotalValue(Map<String, Object> genderData, String category, String gender) {
		long total = 0L;

		for (String level : getHealthLevels()) {
			if (genderData.containsKey(level)) {
				Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);

				if (levelData.containsKey(category)) {
					Map<String, Object> categoryData = (Map<String, Object>) levelData.get(category);

					Object totalValue = categoryData.get(gender + " Total");

					if (totalValue != null) {
						total += totalValue instanceof Integer ? ((Integer) totalValue).longValue() : (Long) totalValue;
					}
				}
			}
		}

		return total;
	}

	private List<String> getHealthLevels() {
		return List.of("Low", "Moderate", "High");
	}

	private List<String> getHealthCategories() {
		return List.of("Outstanding", "You're doing well", "Need to Improve a lot", "Need immediate intervention");
	}

	private void addAgeBandRows(Table table, Map<String, Object> genderData) {
		// Loop through each age band (e.g., "18-20", "21-30"......)
		for (String ageBand : getAgeBands()) {
			table.addCell(createBoldCell(ageBand));

			// Loop through each health category (e.g., "Outstanding!", "You're doing
			// well".......)
			for (String category : getHealthCategories()) {
				String count = extractAgeBandValue(genderData, ageBand, category);
				table.addCell(createDataCell(count));
			}

			// Calculate total count for this age band
			Long totalCount = calculateAgeBandTotal(genderData, ageBand);
			table.addCell(createBoldCell(String.valueOf(totalCount)));
		}
	}

	private List<String> getAgeBands() {
		return List.of("18-20", "21-30", "31-40", "41-50", "51-56");
	}

	private String extractAgeBandValue(Map<String, Object> genderData, String ageBand, String category) {
		// List of health levels to check in order
		List<String> healthLevels = List.of("Low", "Moderate", "High");

		// Loop through each health level to find the value
		for (String level : healthLevels) {
			if (genderData.containsKey(level)) {
				Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);

				// Check if this level contains the category (e.g., "Outstanding!")
				if (levelData.containsKey(category)) {
					Map<String, Long> ageData = (Map<String, Long>) levelData.get(category);

					return String.valueOf(ageData.getOrDefault(ageBand, 0L));
				}
			}
		}

		return "0";
	}

	private Long calculateAgeBandTotal(Map<String, Object> genderData, String ageBand) {
		long total = 0L;

		for (String category : getHealthCategories()) {
			String value = extractAgeBandValue(genderData, ageBand, category);

			long numericValue = value.equals("-") ? 0L : Long.parseLong(value);

			total += numericValue;
		}

		return total;
	}

	private void addOverallTotalRow(Table table, Map<String, Long> overallCounts, Long overallTotal) {
		// Add the "Overall Total" label
		table.addCell(createBoldCell("Overall Total"));

		// Add health category counts for the overall total
		for (String category : getHealthCategories()) {
			Long count = overallCounts.getOrDefault(category, 0L);
			table.addCell(createBoldCell(String.valueOf(count)));
		}
		table.addCell(createBoldCell(String.valueOf(overallTotal)));
	}

	private void addPercentageRows(Table table, Map<String, Map<String, Long>> percentages) {
		// Add percentage rows for "Female %", "Male %", and "Overall %"
		for (String label : List.of("Female %", "Male %", "Overall %")) {
			table.addCell(createBoldCell(label));

			// Add percentage values for each health category
			for (String category : getHealthCategories()) {
				Long percentageValue = percentages.getOrDefault(label, new HashMap<>()).getOrDefault(category, 0L);
				table.addCell(createDataCell(percentageValue + "%"));
			}

			// Add an empty cell for the total column (since percentages don't have a total)
			table.addCell(createDataCell(" "));
		}
	}

	private static Cell createHeaderCell(String text, int colSpan, int rowSpan, DeviceRgb color) {
		return new Cell(rowSpan, colSpan).add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER))
				.setBackgroundColor(color);
	}

	private static Cell createHeaderCell(String text, int colSpan, int rowSpan) {
		return new Cell(rowSpan, colSpan).add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER));
	}

	private static Cell createSubHeaderCell(String text) {
		return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER));
	}

	private static Cell createBoldCell(String text) {
		return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER).setBold());
	}

	private static Cell createDataCell(String text) {
		return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER));
	}

	private Map<String, Long> getSafeLongMap(Object obj) {
		Map<String, Long> safeMap = new HashMap<>();

		if (obj instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() instanceof String key) {
					Long value = parseLongValue(entry.getValue());
					safeMap.put(key, value);
				}
			}
		}

		return safeMap;
	}

	private Map<String, Map<String, Long>> getSafeNestedLongMap(Object obj) {
		Map<String, Map<String, Long>> safeNestedMap = new HashMap<>();

		if (obj instanceof Map<?, ?> outerMap) {
			for (Map.Entry<?, ?> outerEntry : outerMap.entrySet()) {
				if (outerEntry.getKey() instanceof String key) {
					Map<String, Long> innerMap = getSafeLongMap(outerEntry.getValue());
					safeNestedMap.put(key, innerMap);
				}
			}
		}

		return safeNestedMap;
	}

	private Long parseLongValue(Object value) {
		if (value == null) {
			return 0L;
		}

		if (value instanceof Number number) {
			return number.longValue();
		}

		if (value instanceof String str) {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}

		return 0L;
	}

	private Map<String, Map<String, Object>> getSafeNestedObjectMap(Object obj) {
		Map<String, Map<String, Object>> result = new HashMap<>();

		if (obj instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() instanceof String key && entry.getValue() instanceof Map<?, ?> value) {
					result.put(key, (Map<String, Object>) value);
				}
			}
		}

		return result;
	}

	private void HealthStatusChart(Document document, Long assessmentId, Long physicalHealthId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, physicalHealthId,
				headers);

		Map<String, Object> genderAgeBand = (Map<String, Object>) sectionRecord.get("GenderAgeBand");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] categories = { "Outstanding", "You're doing well", "Need immediate intervention",
				"Need to Improve a lot" };
		Color[] colors = { new Color(144, 238, 144), new Color(0, 128, 0), new Color(255, 0, 0),
				new Color(218, 165, 32) };

		// Shortened gender abbreviations
		Map<String, String> genderAbbreviation = new HashMap<>();
		genderAbbreviation.put("Male", "M");
		genderAbbreviation.put("Female", "F");

		String[] ageBands = { "18-20", "21-30", "31-40", "41-50", "51-56" };

		for (String gender : genderAbbreviation.keySet()) {
			Map<String, Object> genderData = (Map<String, Object>) genderAgeBand.get(gender);

			if (genderData != null) {
				for (String category : categories) {
					for (String ageBand : ageBands) {
						try {
							long count = 0L;
							for (String level : getHealthLevels()) {
								Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);
								if (levelData != null) {
									Map<String, Object> categoryData = (Map<String, Object>) levelData.get(category);
									if (categoryData != null) {
										Object value = categoryData.get(ageBand);
										if (value != null) {
											count += value instanceof Integer ? ((Integer) value).longValue()
													: (Long) value;
										}
									}
								}
							}

							// Updated X-axis label format to short gender notation
							String label = genderAbbreviation.get(gender) + " - " + ageBand;
							dataset.addValue(count, category, label);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		try {
			// Create bar chart
			JFreeChart chart = ChartFactory.createBarChart("General Health Status - Age group & Gender wise",
					"Age Group & Gender", "Count", dataset, PlotOrientation.VERTICAL, true, true, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			BarRenderer renderer = (BarRenderer) plot.getRenderer();

			// Set colors for each category
			for (int i = 0; i < colors.length; i++) {
				renderer.setSeriesPaint(i, colors[i]);
			}

			// Adjust X-axis label position to prevent truncation
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotates labels 45 degrees
			domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Reduce font size for clarity

			// Improve legend readability
			chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));

			// Increase chart size for better visualization
			BufferedImage chartImage = chart.createBufferedImage(600, 400);
			ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", chartStream);

			// Add image to PDF
			ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
			Image image = new Image(imageData);
			document.add(image);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void GeneralCorpateHealthStatus(Document document, Long assessmentId, Long physicalHealthId,
			MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException, IOException {
		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, physicalHealthId,
				headers);

		Map<String, Object> healthData = (Map<String, Object>) sectionRecord.get("Percentages");
		Map<String, Double> femaleData = (Map<String, Double>) healthData.get("Female %");
		Map<String, Double> maleData = (Map<String, Double>) healthData.get("Male %");
		Map<String, Double> overallData = (Map<String, Double>) healthData.get("Overall %");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String[] categories = { "Outstanding!", "You're doing well", "Need to Improve a lot",
				"Need immediate intervention" };

		for (String category : categories) {
			dataset.addValue(femaleData.getOrDefault(category, 0.0), "Female %", category);
			dataset.addValue(maleData.getOrDefault(category, 0.0), "Male %", category);
			dataset.addValue(overallData.getOrDefault(category, 0.0), "Overall %", category);
		}

		// Create bar chart
		JFreeChart chart = ChartFactory.createBarChart("General Corporate Health Status", "Category", "Percentage",
				dataset);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, new Color(30, 100, 200)); // Female % - Blue
		renderer.setSeriesPaint(1, new Color(200, 50, 50)); // Male % - Red
		renderer.setSeriesPaint(2, new Color(50, 150, 50)); // Overall % - Green

		// Convert chart to image
		BufferedImage chartImage = chart.createBufferedImage(600, 400);
		ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
		ChartUtils.writeBufferedImageAsPNG(chartStream, chartImage);

		// Add chart image to PDF
		ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
		Image image = new Image(imageData);
		image.setHorizontalAlignment(HorizontalAlignment.CENTER);
		document.add(image);

		document.add(new Paragraph("Key Observations for XXX to take note under this section:").setUnderline()
				.setFontSize(12));

		document.add(new Paragraph(
				"1. 28 (1.11% of the total participants) members claimed that they know their health is poor\n"
						+ "2. 285 (11.26% of the total participants) members feel that they don’t have a good immunity\n"
						+ "3. 444 (17.55% of the total participants) members confirmed that they don’t indulge in any physical activity at all\n"
						+ "4. 480 (18.97% of the total participants) members confirmed that they ignore symptoms at early stages and avoid seeking any medical advice from professionals\n"
						+ "5. 378 (14.94% of the total participants) members are not maintaining their weight\n"
						+ "6. 180 (7.11% of the total participants) members answered that they take more time to recover from any illness than usual\n"
						+ "7. 141 (5.57% of the total participants) members claim that they always feel tired towards end of the day"));

		document.add(new Paragraph("Recommendations to XXX under this section:").setUnderline().setFontSize(12));

		document.add(new Paragraph(
				"1. Organize sessions on Common Health Disorders by expert panel of Doctors (Orthopaedic,Physio Therapists, etc.)\n"
						+ "2. Organize Yoga or any other fitness activity at workplace\n"
						+ "3. Ergonomic evaluation of office environment\n"
						+ "4. Introduce tele - counseling or health coach programs for counseling employees on their Nutrition needs."));

	}

	private void addMentalHealthStatusTable(Document document, Long assessmentId, Long mentalHealthId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		document.add(new Paragraph("Mental Health Status").setBold().setFontSize(14)
				.setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

		float[] columnWidths = { 3, 2, 2, 2, 2, 2 };
		Table table = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));

		// Header rows
		table.addCell(createHeaderCell("Gender / Age Band", 1, 2));
		table.addCell(createHeaderCell("Low", 2, 1, new DeviceRgb(160, 212, 104))); // Green
		table.addCell(createHeaderCell("Moderate", 1, 1, new DeviceRgb(255, 206, 84))); // Yellow
		table.addCell(createHeaderCell("High", 1, 1, new DeviceRgb(237, 85, 101))); // Red
		table.addCell(createHeaderCell("Total", 1, 2));

		// Subheaders
		table.addCell(createSubHeaderCell("Outstanding!"));
		table.addCell(createSubHeaderCell("You're doing well"));
		table.addCell(createSubHeaderCell("Need to Improve a lot"));
		table.addCell(createSubHeaderCell("Need Immediate intervention"));

		addDynamicHealthStatusRows(table, assessmentId, mentalHealthId, headers);

		document.add(table);
	}

	private void MentalHealthStatusChart(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, sectionId,
				headers);
		Map<String, Object> genderAgeBand = (Map<String, Object>) sectionRecord.get("GenderAgeBand");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] categories = { "Outstanding", "You're doing well", "Need immediate intervention",
				"Need to Improve a lot" };
		Color[] colors = { new Color(144, 238, 144), new Color(0, 128, 0), new Color(255, 0, 0),
				new Color(218, 165, 32) };

		// Using short gender abbreviations
		Map<String, String> genderAbbreviation = new HashMap<>();
		genderAbbreviation.put("Male", "M");
		genderAbbreviation.put("Female", "F");

		String[] ageBands = { "18-20", "21-30", "31-40", "41-50", "51-56" };

		for (String gender : genderAbbreviation.keySet()) {
			Map<String, Object> genderData = (Map<String, Object>) genderAgeBand.get(gender);

			if (genderData != null) {
				for (String category : categories) {
					for (String ageBand : ageBands) {
						try {
							long count = 0L;
							for (String level : getHealthLevels()) {
								Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);
								if (levelData != null) {
									Map<String, Object> categoryData = (Map<String, Object>) levelData.get(category);
									if (categoryData != null) {
										Object value = categoryData.get(ageBand);
										if (value != null) {
											count += value instanceof Integer ? ((Integer) value).longValue()
													: (Long) value;
										}
									}
								}
							}

							// Updated X-axis label format
							String label = genderAbbreviation.get(gender) + " - " + ageBand;
							dataset.addValue(count, category, label);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		try {
			// Create bar chart
			JFreeChart chart = ChartFactory.createBarChart("Mental Health Status - Age group & Gender wise", "Category",
					"Count", dataset, PlotOrientation.VERTICAL, true, true, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			BarRenderer renderer = (BarRenderer) plot.getRenderer();

			// Set colors for each category
			for (int i = 0; i < colors.length; i++) {
				renderer.setSeriesPaint(i, colors[i]);
			}

			// Adjust X-axis label position to prevent truncation
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotates labels 45 degrees
			domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Reduce font size for clarity

			// Convert chart to image
			BufferedImage chartImage = chart.createBufferedImage(600, 400);
			ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", chartStream);

			// Add image to PDF
			ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
			Image image = new Image(imageData);
			document.add(image);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void MentalHealthStatus(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException, IOException {
		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, sectionId,
				headers);

		Map<String, Object> healthData = (Map<String, Object>) sectionRecord.get("Percentages");
		Map<String, Double> femaleData = (Map<String, Double>) healthData.get("Female %");
		Map<String, Double> maleData = (Map<String, Double>) healthData.get("Male %");
		Map<String, Double> overallData = (Map<String, Double>) healthData.get("Overall %");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String[] categories = { "Outstanding!", "You're doing well", "Need to Improve a lot",
				"Need immediate intervention" };

		for (String category : categories) {
			dataset.addValue(femaleData.getOrDefault(category, 0.0), "Female %", category);
			dataset.addValue(maleData.getOrDefault(category, 0.0), "Male %", category);
			dataset.addValue(overallData.getOrDefault(category, 0.0), "Overall %", category);
		}

		// Create bar chart
		JFreeChart chart = ChartFactory.createBarChart("Mental Health Status", "Category", "Percentage", dataset);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, new Color(30, 100, 200)); // Female % - Blue
		renderer.setSeriesPaint(1, new Color(200, 50, 50)); // Male % - Red
		renderer.setSeriesPaint(2, new Color(50, 150, 50)); // Overall % - Green

		// Convert chart to image
		BufferedImage chartImage = chart.createBufferedImage(600, 400);
		ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
		ChartUtils.writeBufferedImageAsPNG(chartStream, chartImage);

		// Add chart image to PDF
		ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
		Image image = new Image(imageData);
		image.setHorizontalAlignment(HorizontalAlignment.CENTER);
		document.add(image);
		document.add(new Paragraph("\n"));
		document.add(new Paragraph("\n"));

		document.add(new Paragraph("Key Observations for XXX to take note under this section:").setUnderline().setBold()
				.setFontSize(12));

		document.add(new Paragraph(
				"1. 444 (17.55% of the total participants) members vent their anger / frustration which can hurt others\n"
						+ "2. 166 (6.56% of the total participants) members feel they don’t have any emotional support\n"
						+ "3. 23 (0.91% of the total participants) members are careless about their responsibilities and actions\n"
						+ "4. 157 (6.21% of the total participants) members don’t know if they would get any emotional support if they need\n"
						+ "5. 353 (13.95% of the total participants) members think their emotion get the better of them and they act without thinking\n"
						+ "6. 893 (35.30% of the total participants) members think they felt tensed, anxious or depressed on\n"
						+ "several occasions.\n "
						+ "7. 304 (12.02% of the total participants) members think they felt tensed, anxious or depressed Almost every day\n"
						+ "8. 583 (23.04% of the total participants) members think they felt tensed, anxious or depressed on rare occasions."));

		document.add(
				new Paragraph("Recommendations to XXX under this section:").setUnderline().setBold().setFontSize(12));

		document.add(new Paragraph(
				"1. Conduct Sessions on How to Manage Stress effectively/ Various ways to combat stress)\n"
						+ "2. Options to be explored on imparting training on Mental Health Strategies to the higher grades.\n"
						+ "3. Encourage positive communication in the workplace\n"
						+ "4. Offer rebates for visit to a counselor, if required"
						+ "5. Adopt / develop mental health plans – counseling sessions, stress management techniques, relaxation therapies, etc."));

	}

	private void addDigitalHealthStatusTable(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		document.add(new Paragraph("Digital Health Status").setBold().setFontSize(14).setUnderline()
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(10));

		float[] columnWidths = { 3, 2, 2, 2, 2, 2 };
		Table table = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));

		// Header rows
		table.addCell(createHeaderCell("Gender / Age Band", 1, 2));
		table.addCell(createHeaderCell("Low", 2, 1, new DeviceRgb(160, 212, 104))); // Green
		table.addCell(createHeaderCell("Moderate", 1, 1, new DeviceRgb(255, 206, 84))); // Yellow
		table.addCell(createHeaderCell("High", 1, 1, new DeviceRgb(237, 85, 101))); // Red
		table.addCell(createHeaderCell("Total", 1, 2));

		// Subheaders
		table.addCell(createSubHeaderCell("Outstanding!"));
		table.addCell(createSubHeaderCell("You're doing well"));
		table.addCell(createSubHeaderCell("Need to Improve a lot"));
		table.addCell(createSubHeaderCell("Need Immediate intervention"));

		addDynamicHealthStatusRows(table, assessmentId, sectionId, headers);

		document.add(table);
	}

	private void DigitalHealthStatusChart(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, sectionId,
				headers);
		Map<String, Object> genderAgeBand = (Map<String, Object>) sectionRecord.get("GenderAgeBand");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] categories = { "Outstanding", "You're doing well", "Need immediate intervention",
				"Need to Improve a lot" };
		Color[] colors = { new Color(144, 238, 144), new Color(0, 128, 0), new Color(255, 0, 0),
				new Color(218, 165, 32) };

		// Using short gender abbreviations
		Map<String, String> genderAbbreviation = new HashMap<>();
		genderAbbreviation.put("Male", "M");
		genderAbbreviation.put("Female", "F");

		String[] ageBands = { "18-20", "21-30", "31-40", "41-50", "51-56" };

		for (String gender : genderAbbreviation.keySet()) {
			Map<String, Object> genderData = (Map<String, Object>) genderAgeBand.get(gender);

			if (genderData != null) {
				for (String category : categories) {
					for (String ageBand : ageBands) {
						try {
							long count = 0L;
							for (String level : getHealthLevels()) {
								Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);
								if (levelData != null) {
									Map<String, Object> categoryData = (Map<String, Object>) levelData.get(category);
									if (categoryData != null) {
										Object value = categoryData.get(ageBand);
										if (value != null) {
											count += value instanceof Integer ? ((Integer) value).longValue()
													: (Long) value;
										}
									}
								}
							}

							// Updated X-axis label format
							String label = genderAbbreviation.get(gender) + " - " + ageBand;
							dataset.addValue(count, category, label);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		try {
			// Create bar chart
			JFreeChart chart = ChartFactory.createBarChart("Digital Health Status - Age group & Gender wise",
					"Category", "Count", dataset, PlotOrientation.VERTICAL, true, true, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			BarRenderer renderer = (BarRenderer) plot.getRenderer();

			// Set colors for each category
			for (int i = 0; i < colors.length; i++) {
				renderer.setSeriesPaint(i, colors[i]);
			}

			// Adjust X-axis label position to prevent truncation
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotates labels 45 degrees
			domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Reduce font size for clarity

			// Convert chart to image
			BufferedImage chartImage = chart.createBufferedImage(600, 400);
			ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", chartStream);

			// Add image to PDF
			ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
			Image image = new Image(imageData);
			document.add(image);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addSleepStatusTable(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		document.add(new Paragraph("Sleep Status").setBold().setFontSize(14).setUnderline()
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(10));

		float[] columnWidths = { 3, 2, 2, 2, 2, 2 };
		Table table = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));

		// Header rows
		table.addCell(createHeaderCell("Gender / Age Band", 1, 2));
		table.addCell(createHeaderCell("Low", 2, 1, new DeviceRgb(160, 212, 104))); // Green
		table.addCell(createHeaderCell("Moderate", 1, 1, new DeviceRgb(255, 206, 84))); // Yellow
		table.addCell(createHeaderCell("High", 1, 1, new DeviceRgb(237, 85, 101))); // Red
		table.addCell(createHeaderCell("Total", 1, 2));

		// Subheaders
		table.addCell(createSubHeaderCell("Outstanding!"));
		table.addCell(createSubHeaderCell("You're doing well"));
		table.addCell(createSubHeaderCell("Need to Improve a lot"));
		table.addCell(createSubHeaderCell("Need Immediate intervention"));

		addDynamicHealthStatusRows(table, assessmentId, sectionId, headers);

		document.add(table);
	}

	private void SleepStatusChart(Document document, Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {

		Map<String, Object> sectionRecord = assessmentUtil.getSectionRecordsCalculation(assessmentId, sectionId,
				headers);
		Map<String, Object> genderAgeBand = (Map<String, Object>) sectionRecord.get("GenderAgeBand");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] categories = { "Outstanding", "You're doing well", "Need immediate intervention",
				"Need to Improve a lot" };
		Color[] colors = { new Color(144, 238, 144), new Color(0, 128, 0), new Color(255, 0, 0),
				new Color(218, 165, 32) };

		// Using short gender abbreviations
		Map<String, String> genderAbbreviation = new HashMap<>();
		genderAbbreviation.put("Male", "M");
		genderAbbreviation.put("Female", "F");

		String[] ageBands = { "18-20", "21-30", "31-40", "41-50", "51-56" };

		for (String gender : genderAbbreviation.keySet()) {
			Map<String, Object> genderData = (Map<String, Object>) genderAgeBand.get(gender);

			if (genderData != null) {
				for (String category : categories) {
					for (String ageBand : ageBands) {
						try {
							long count = 0L;
							for (String level : getHealthLevels()) {
								Map<String, Object> levelData = (Map<String, Object>) genderData.get(level);
								if (levelData != null) {
									Map<String, Object> categoryData = (Map<String, Object>) levelData.get(category);
									if (categoryData != null) {
										Object value = categoryData.get(ageBand);
										if (value != null) {
											count += value instanceof Integer ? ((Integer) value).longValue()
													: (Long) value;
										}
									}
								}
							}

							// Updated X-axis label format
							String label = genderAbbreviation.get(gender) + " - " + ageBand;
							dataset.addValue(count, category, label);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		try {
			// Create bar chart
			JFreeChart chart = ChartFactory.createBarChart("Sleep Status - Age group & Gender wise", "Category",
					"Count", dataset, PlotOrientation.VERTICAL, true, true, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			BarRenderer renderer = (BarRenderer) plot.getRenderer();

			// Set colors for each category
			for (int i = 0; i < colors.length; i++) {
				renderer.setSeriesPaint(i, colors[i]);
			}

			// Adjust X-axis label position to prevent truncation
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotates labels 45 degrees
			domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Reduce font size for clarity

			// Convert chart to image
			BufferedImage chartImage = chart.createBufferedImage(600, 400);
			ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", chartStream);

			// Add image to PDF
			ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
			Image image = new Image(imageData);
			document.add(image);

			document.add(new Paragraph("\n"));

			document.add(new Paragraph("Key Observations for XXX to take note under this section:").setUnderline()
					.setBold().setFontSize(12));

			document.add(new Paragraph("1. 1443 (57.04% of the total participants) members Sleep late\n"
					+ "2. 660 (26.09% of the total participants) members do not get adequate sleep, less than 6 hrs.\n"
					+ "3. 994 (45.49% of the total participants) members have accepted that their sleep quality is not good\n"
					+ "4. 1187 (46.92% of the total participants) members receive notifications more than 10 times a day"));

			document.add(new Paragraph("Recommendations to XXX under this section:").setUnderline().setBold()
					.setFontSize(12));

			document.add(new Paragraph("1. Organize sessions on Meditation and Stress Management\n"
					+ "2. Ask experts to address members on how to improve sleeping habits – Do’s & Don’ts\n"
					+ "3. Ask members to seek professional advice\n"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addConclusion(Document document) {
		DeviceRgb blueColor = new DeviceRgb(0, 0, 255);
		document.add(new Paragraph("Conclusion").setFontColor(blueColor).setUnderline().setFontSize(16)
				.setTextAlignment(TextAlignment.LEFT));

		document.add(new Paragraph(
				"A health risk assessment is one of the most popular tools used for assessing one’s health and is often "
						+ "the first step in the multi-component health promotion programs.\n\n"
						+ "As per our findings in the survey, we can safely say that the employees are willing to quit their bad habits "
						+ "or alter their lifestyle for a better and healthy life, however they need proper guidance and they have also "
						+ "shown inclination to pay for this guidance. The changes in their lifestyle habits will bring about a change "
						+ "in the behavior of the employees and which will have a positive impact on every facet of their life "
						+ "whether professional or personal.\n\n"
						+ "We strongly feel that there is a need for altering certain behavior such as controlling time spent by "
						+ "employees on digital platforms or devices at workplace. This will lead to an instant change in the way the "
						+ "members interact with each other at workplace or in their personal space. This will ultimately be helpful "
						+ "for the employees to shed any unnecessary stress and will increase their social interaction. Moreover, we "
						+ "strongly recommend XXX to adopt a mental wellness strategy which should include de-addiction "
						+ "programs as well.\n\n"
						+ "At the end a happy and healthy employee is the key ingredient to achieve consumer satisfaction and "
						+ "benefit you in the long run.\n\n"
						+ "We have submitted our recommendations under each section in the above report. In case you need any "
						+ "additional clarification or information or assistance on the same, please do let us know and we will be "
						+ "happy to assist you.\n\n"
						+ "We once again thank you for your kind understanding and support during the initial glitches we faced. We "
						+ "are truly proud to be associated with an esteemed organization such as yours.\n\n"
						+ "We look forward to working with you again.")
				.setTextAlignment(TextAlignment.JUSTIFIED));

	}

}
