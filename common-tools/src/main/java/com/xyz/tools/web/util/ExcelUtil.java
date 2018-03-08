package com.xyz.tools.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
** @author Administrator
** @version 2017年9月14日下午2:26:10
** @Description
*/
public class ExcelUtil {
	
	// 文件名
		private String fileName;
		// 显示的导出表的标题
		private String title;
		// 导出表的列名
		private String[] rowName;

		private List<Object[]> dataList = new ArrayList<Object[]>();

		HttpServletResponse response;

		// 构造方法，传入要导出的数据
		public ExcelUtil(String title, String[] rowName, List<Object[]> dataList, HttpServletResponse response) {
			this.dataList = dataList;
			this.rowName = rowName;
			this.title = title;
			this.response = response;
		}

		// 构造方法，传入要导出的数据
		public ExcelUtil(String fileName, String title, String[] rowName, List<Object[]> dataList,
				HttpServletResponse response) {
			this.fileName = fileName;
			this.dataList = dataList;
			this.rowName = rowName;
			this.title = title;
			this.response = response;
		}

		/*
		 * 导出数据
		 */
		public void export() throws Exception {
			try {
				HSSFWorkbook workbook = new HSSFWorkbook(); // 创建工作簿对象
				HSSFSheet sheet = workbook.createSheet(title); // 创建工作表

				// 产生表格标题行
				HSSFRow rowm = sheet.createRow(0);
				HSSFCell cellTiltle = rowm.createCell(0);

				// sheet样式定义【getColumnTopStyle()/getStyle()均为自定义方法 - 在下面 - 可扩展】
				HSSFCellStyle columnTopStyle = this.getColumnTopStyle(workbook);// 获取列头样式对象
				HSSFCellStyle style = this.getStyle(workbook); // 单元格样式对象

				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, (rowName.length - 1)));
				cellTiltle.setCellStyle(columnTopStyle);
				cellTiltle.setCellValue(title);

				// 定义所需列数
				int columnNum = rowName.length;
				HSSFRow rowRowName = sheet.createRow(1); // 在索引2的位置创建行(最顶端的行开始的第二行)

				// 将列头设置到sheet的单元格中
				for (int n = 0; n < columnNum; n++) {
					HSSFCell cellRowName = rowRowName.createCell(n); // 创建列头对应个数的单元格
					cellRowName.setCellType(HSSFCell.CELL_TYPE_STRING); // 设置列头单元格的数据类型
					HSSFRichTextString text = new HSSFRichTextString(rowName[n]);
					cellRowName.setCellValue(text); // 设置列头单元格的值
					cellRowName.setCellStyle(columnTopStyle); // 设置列头单元格样式
				}

				// 将查询出的数据设置到sheet对应的单元格中
				for (int i = 0; i < dataList.size(); i++) {

					Object[] obj = dataList.get(i);// 遍历每个对象
					HSSFRow row = sheet.createRow(i + 2);// 创建所需的行数

					for (int j = 0; j < obj.length; j++) {
						HSSFCell cell = null; // 设置单元格的数据类型
						// if(j == 0){
						// cell = row.createCell(j,HSSFCell.CELL_TYPE_NUMERIC);
						// cell.setCellValue(i+1);
						// }else{
						cell = row.createCell(j, HSSFCell.CELL_TYPE_STRING);
						if (!"".equals(obj[j]) && obj[j] != null) {
							cell.setCellValue(obj[j].toString()); // 设置单元格的值
						}
						// }
						cell.setCellStyle(style); // 设置单元格样式
					}
				}
				// 让列宽随着导出的列长自动适应
				for (int colNum = 0; colNum < columnNum; colNum++) {
					int columnWidth = sheet.getColumnWidth(colNum) / 256;
					for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
						HSSFRow currentRow;
						// 当前行未被使用过
						if (sheet.getRow(rowNum) == null) {
							currentRow = sheet.createRow(rowNum);
						} else {
							currentRow = sheet.getRow(rowNum);
						}
						if (currentRow.getCell(colNum) != null) {
							HSSFCell currentCell = currentRow.getCell(colNum);
							if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
								int length = currentCell.getStringCellValue().getBytes().length;
								if (columnWidth < length) {
									columnWidth = length + 2;
								}
							}
						}
					}
					if (colNum == 0) {
						sheet.setColumnWidth(colNum, (columnWidth - 2) * 256);
					} else {
						sheet.setColumnWidth(colNum, (columnWidth + 4) * 256);
					}
				}

				if (workbook != null) {
					try {
						if (fileName == null) {
							fileName = title+"-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
						}
						// String headStr = "attachment; filename=\"" + fileName +
						// "\"";
						// response.setContentType("APPLICATION/OCTET-STREAM");
						// response.setHeader("Content-Disposition", headStr);
						response.setContentType("application/vnd.ms-excel");
						response.setHeader("Content-Disposition",
								"attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
						OutputStream out = response.getOutputStream();
						workbook.write(out);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		/*
		 * 列头单元格样式
		 */
		public HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {

			// 设置字体
			HSSFFont font = workbook.createFont();
			// 设置字体大小
			font.setFontHeightInPoints((short) 11);
			// 字体加粗
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			// 设置字体名字
			font.setFontName("Courier New");
			// 设置样式;
			HSSFCellStyle style = workbook.createCellStyle();
			// 设置底边框;
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			// 设置底边框颜色;
			style.setBottomBorderColor(HSSFColor.BLACK.index);
			// 设置左边框;
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			// 设置左边框颜色;
			style.setLeftBorderColor(HSSFColor.BLACK.index);
			// 设置右边框;
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			// 设置右边框颜色;
			style.setRightBorderColor(HSSFColor.BLACK.index);
			// 设置顶边框;
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			// 设置顶边框颜色;
			style.setTopBorderColor(HSSFColor.BLACK.index);
			// 在样式用应用设置的字体;
			style.setFont(font);
			// 设置自动换行;
			style.setWrapText(false);
			// 设置水平对齐的样式为居中对齐;
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			// 设置垂直对齐的样式为居中对齐;
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

			return style;

		}

		/*
		 * 列数据信息单元格样式
		 */
		public HSSFCellStyle getStyle(HSSFWorkbook workbook) {
			// 设置字体
			HSSFFont font = workbook.createFont();
			// 设置字体大小
			// font.setFontHeightInPoints((short)10);
			// 字体加粗
			// font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			// 设置字体名字
			font.setFontName("Courier New");
			// 设置样式;
			HSSFCellStyle style = workbook.createCellStyle();
			// 设置底边框;
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			// 设置底边框颜色;
			style.setBottomBorderColor(HSSFColor.BLACK.index);
			// 设置左边框;
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			// 设置左边框颜色;
			style.setLeftBorderColor(HSSFColor.BLACK.index);
			// 设置右边框;
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			// 设置右边框颜色;
			style.setRightBorderColor(HSSFColor.BLACK.index);
			// 设置顶边框;
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			// 设置顶边框颜色;
			style.setTopBorderColor(HSSFColor.BLACK.index);
			// 在样式用应用设置的字体;
			style.setFont(font);
			// 设置自动换行;
			style.setWrapText(false);
			// 设置水平对齐的样式为居中对齐;
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			// 设置垂直对齐的样式为居中对齐;
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

			return style;

		}
		
		
		/**
	     * 由指定的Sheet导出至List
	     * 
	     * @param workbook
	     * @param sheetNum
	     * @return
	     * @throws IOException
	     */
	    public static List<String[]> exportListFromExcel(Workbook workbook,
	            int sheetNum)
	    {

	        Sheet sheet = workbook.getSheetAt(sheetNum);

	        // 解析公式结果
	        FormulaEvaluator evaluator = workbook.getCreationHelper()
	                .createFormulaEvaluator();

	        List<String[]> list = new ArrayList<String[]>();
	        
	        int minRowIx = sheet.getFirstRowNum();
	        int maxRowIx = sheet.getLastRowNum();
	       
	        for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++)
	        {
	            Row row = sheet.getRow(rowIx);
	            short minColIx = row.getFirstCellNum();
	            short maxColIx = row.getLastCellNum();
	            String[] rowdate = new String[maxColIx];
	            for (short colIx = minColIx; colIx <= maxColIx; colIx++)
	            {
	                Cell cell = row.getCell(new Integer(colIx));
	                CellValue cellValue = evaluator.evaluate(cell);
	                if (cellValue == null)
	                {
	                	if(colIx==minColIx){
	                		rowdate =null;
	                	}
	                    continue;
	                }
	                Object result = null;
	                switch (cell.getCellType())
	                {
	                   case Cell.CELL_TYPE_BOOLEAN:
	                       result = cell.getBooleanCellValue();
	                    break;
	                    case HSSFCell.CELL_TYPE_NUMERIC:
	                        if (HSSFDateUtil.isCellDateFormatted(cell))
	                        {   
	                            // 处理日期格式、时间格式
	                            SimpleDateFormat sdf = null;
	                            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm"))
	                            {
	                                sdf = new SimpleDateFormat("HH:mm");
	                            }
	                            else if(cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm:ss"))
	                            {
	                                sdf = new SimpleDateFormat("HH:mm:ss");
	                            }
	                            else
	                            {
	                                sdf = new SimpleDateFormat("yyyy-MM-dd");
	                            }
	                            Date date = cell.getDateCellValue();
	                            result = sdf.format(date);
	                        }
	                        else if (cell.getCellStyle().getDataFormat() == 58)
	                        {
	                            // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
	                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	                            double value = cell.getNumericCellValue();
	                            Date date = org.apache.poi.ss.usermodel.DateUtil
	                                    .getJavaDate(value);
	                            result = sdf.format(date);
	                        }
	                        else
	                        {
	                            double value = cell.getNumericCellValue();
	                            CellStyle style = cell.getCellStyle();
	                            DecimalFormat format = new DecimalFormat();
	                            String temp = style.getDataFormatString();
	                            // 单元格设置成常规
	                            if (temp.equals("General"))
	                            {
	                               // format.applyPattern("#");
	                            }
	                            result = format.format(value);
	                        }
	                        break;
	                    case HSSFCell.CELL_TYPE_STRING:
	                        result = cell.getRichStringCellValue().toString();
	                        break;
	                    case Cell.CELL_TYPE_FORMULA:
	                        break;
	                    case Cell.CELL_TYPE_BLANK:
	                        break;
	                    case Cell.CELL_TYPE_ERROR:
	                        break;
	                    default:
	                        break;
	                }
	                System.out.println(colIx);
	                System.out.println(result);
	                rowdate[colIx] = ""+ (result == null ? "" : result);            
	            }
	            if(rowdate!=null)
	            list.add(rowdate);
	        }
	        return list;
	    }
	    

}