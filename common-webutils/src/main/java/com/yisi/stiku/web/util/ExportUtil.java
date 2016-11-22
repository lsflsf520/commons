package com.yisi.stiku.web.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shangfeng
 *
 */
public class ExportUtil {

	private final static Logger LOG = LoggerFactory.getLogger(ExportUtil.class);

	public static HSSFWorkbook genExcel(List<String> titleList, List<List<String>> rowValList, String sheetName) {

		HSSFWorkbook workbook = new HSSFWorkbook();// 创建一个Excel文件

		return genExcel(workbook, titleList, rowValList, sheetName);
	}

	public static HSSFWorkbook genExcel(HSSFWorkbook workbook, List<String> titleList, List<List<String>> rowValList,
			String sheetName) {

		HSSFSheet sheet = StringUtils.isBlank(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);// 创建一个Excel的Sheet
		// 写入标题行
		HSSFRow titleRow = sheet.createRow(0);
		int colIndex = 0;
		for (String title : titleList) {
			HSSFCell cell = titleRow.createCell(colIndex++);
			cell.setCellValue(title);
		}
		int rowIndex = 1;
		for (List<String> rowVals : rowValList) {
			HSSFRow dataRow = sheet.createRow(rowIndex++);
			colIndex = 0;
			for (String val : rowVals) {
				HSSFCell cell = dataRow.createCell(colIndex++);
				cell.setCellValue(val);
			}
		}

		return workbook;
	}

	public static void writeExcel(HSSFWorkbook workbook, String filePath) {

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filePath));
			workbook.write(fos);
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (fos != null) {
				try {
					fos.flush();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					try {
						fos.close();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	public static void writeHttpResponse(HttpServletResponse response, HSSFWorkbook workbook, String fileName) {

		OutputStream os = null;
		try {
			// 清空response
			response.reset();
			// 设置response的Header
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String(fileName.getBytes("UTF-8"), "ISO8859-1") + ".xls");
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			os = new BufferedOutputStream(
					response.getOutputStream());
			workbook.write(response.getOutputStream());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (os != null) {
				try {
					os.flush();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					try {
						os.close();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	public static void main(String[] args) {

		HSSFWorkbook wb = genExcel(Arrays.asList("姓名", "性别", "年龄"),
				Arrays.asList(Arrays.asList("刘尚风", "男", "28"), Arrays.asList("王珏", "女", "29")), "名单");

		wb = genExcel(wb, Arrays.asList("班级", "男生", "女生"),
				Arrays.asList(Arrays.asList("1班", "20", "18"), Arrays.asList("2班", "25", "29")), "班级概览");
		writeExcel(wb, "D:/name.xls");

	}

}
