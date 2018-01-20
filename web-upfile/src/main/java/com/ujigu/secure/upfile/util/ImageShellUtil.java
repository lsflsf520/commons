package com.ujigu.secure.upfile.util;


import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.CmdExecUtil;
import com.ujigu.secure.upfile.bean.ImgPosition;




/**
 * 
 * @author lsf
 * 
 */
public class ImageShellUtil {

	private final static Logger LOG = LoggerFactory
			.getLogger(ImageShellUtil.class);

	public static final String W_H_SPLITER = "x";
	public static final String COMPRESSION_IDENTITY = "comp";

	public static final String TMP_SUFFIX = ".tmp.jpg";
	public static final String GIF_TMP_FLAG = "_frm";

	private static final String CONVERT_CMD_PATH = BaseConfig.getValue("img.convert.cmd.path", "/usr/bin/convert");

	/**
	 * 
	 * @param fromImg
	 * @param toImg
	 * @param widthPx
	 * @param heightPx
	 */
	public static void scaleImg(String fromImg, String toImg, int widthPx,
			int heightPx)  {
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg + " -resize " + widthPx
				+ "x" + heightPx + " " + toImg;

		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("SCALE_ERR", "exec " + cmdLineStr, e);
		}
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param widthPx
	 *            被缩放的宽度
	 * @param heightPx
	 *            被缩放的高度
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String scaleImg(String fromImg, int widthPx, int heightPx) {
		final String size = widthPx + W_H_SPLITER + heightPx;
		String filename = FilenameUtils.getName(fromImg);
		try {
			String baseDir = FilenameUtils.getFullPath(fromImg);

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + size + "." + ext;

			String filePath = baseDir + filename;

			scaleImg(fromImg, filePath, widthPx, heightPx);

		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",widthPx:" + widthPx
					+ ",heightPx:" + heightPx,e);

			return FilenameUtils.getName(fromImg);
		}

		return filename; // 压缩成功，则将resultDir+"/"+文件名返回
	}
	
	/**
	 * 
	 * @param fromImg 原图路径
	 * @param watermarkImg 水印图片路径
	 * @param toImg 目标图片路径
	 * @param position 水印放置位置
	 * @param offsetStr 对水印位置进行像素微调
	 */
	public static void compositeImg(String fromImg, String watermarkImg, String toImg, ImgPosition position, String offsetStr){
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg + " " + watermarkImg + " -gravity " + position.name() + (StringUtils.isNotBlank(offsetStr) ? " -geometry " + offsetStr : "" )+ " -composite " + toImg;
		
		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("COMPOSITE_ERR", "exec " + cmdLineStr, e);
		}
	}
	
	/**
	 * 
	 * @param fromImg 原图路径
	 * @param watermarkImg 水印图片路径
	 * @param position 水印放置位置
	 * @param offsetStr 对水印位置进行像素微调
	 * @return 返回原图文件名
	 */
	public static String compositeImg(String fromImg, String watermarkImg, ImgPosition position, String offsetStr){
		try{
			compositeImg(fromImg, watermarkImg, fromImg, position, offsetStr);
		} catch (Exception e){
			LOG.error("fromImg:" + fromImg + ",watermarkImg:" + watermarkImg
					+ ",position:" + position + ",offsetStr:" + offsetStr,e);
		}
		
		return FilenameUtils.getName(fromImg);
	}
	
	/**
	 * 
	 * @param word 用于生成图片的文字
	 * @param toImg 目标图片路径
	 * @param width 目标图片宽度
	 * @param height 目标图片高度
	 * @param fontFile 生成图片时，文字使用的字体文件路径
	 * @param fontSize 文字大小
	 * @param fontColor 文字颜色
	 * @param linespace 行间距
	 */
	public static void word2Img(String word, String toImg, int width, int height, String fontFile, int fontSize, String fontColor, int linespace){
		// -interline-spacing 这个参数貌似只能在 ImageMagick 6.5.7-10 这个版本中才能使用，其它版本都会报错
		String cmdLineStr = CONVERT_CMD_PATH + " -size " + width + "x" + height + " -interline-spacing " + linespace + " -encoding utf8 -background white -fill \"" + fontColor + "\" -font " + fontFile + " -gravity center caption:\"" + word + "\" -pointsize " + fontSize + " " + toImg;
		
		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("COMPOSITE_ERR", "exec " + cmdLineStr, e);
		}
	}
	
	/**
	 * 
	 * @param fromImg 原图路径
	 * @param toImg 目标图片路径
	 * @param color 需要被填充的背景颜色
	 * @param position 位置
	 * @param width 目标图片宽度
	 * @param height 目标图片高度
	 */
	public static void fillBgWithColor(String fromImg, String toImg, String bgColor, ImgPosition position, int width, int height){
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg  + " -resize " + width + "x" + height + " -background " + bgColor + " -gravity " + position.name() + " -extent " + width + "x" + height + " " + toImg;
		
		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("FILL_COLOR_ERR", "exec " + cmdLineStr, e);
		}
	}
	
	/**
	 * 按照指定的宽度和高度对图片的背景进行补白
	 * @param fromImg 原图路径
	 * @param position 位置
	 * @param width 目标图片宽度
	 * @param height 目标图片高度
	 * @return 如果操作成功，则返回成功补白后图片的文件名；否则返回原图文件名
	 */
	public static String fillBgWithColor(String fromImg, ImgPosition position, String color, int width, int height){
		final String size = width + W_H_SPLITER + height;
		String filename = FilenameUtils.getName(fromImg);
		try {
			String baseDir = FilenameUtils.getFullPath(fromImg);

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + size + "." + ext;

			String filePath = baseDir + filename;

			fillBgWithColor(fromImg, filePath, color, position, width, height);

		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",widthPx:" + width
					+ ",heightPx:" + height,e);

			return FilenameUtils.getName(fromImg);
		}

		return filename; // 压缩成功，则将resultDir+"/"+文件名返回
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param toImg
	 *            被压缩后的图片路径
	 * @param quality
	 *            压缩质量，从0到100之间的数字
	 * @throws MagickException
	 *             压缩不成功，可能会跑出该异常
	 */
	public static void compressionImg(String fromImg, String toImg, int quality)
			{
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg
				+ " -compress JPEG -quality " + quality + " " + toImg;

		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("COMPRESS_ERR", "exec " + cmdLineStr, e);
		}
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param quality
	 *            压缩质量，从0到100之间的数字
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String compressionImg(String fromImg, int quality) {
		String filename = FilenameUtils.getName(fromImg);
		try {
			String baseDir = FilenameUtils.getFullPath(fromImg);

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + COMPRESSION_IDENTITY + "." + ext;

			String filePath = baseDir + filename;

			compressionImg(fromImg, filePath, quality);

		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",quality:" + quality, e);

			return FilenameUtils.getName(fromImg);
		}

		return filename; // 压缩成功，则将resultDir+"/"+文件名返回
	}
	
	/**
	 * 从gif图片中抓取指定的帧
	 * @param gifImgPath gif图片路径
	 * @param toImg 目标图片路径
	 * @param index  指定帧的索引，从0开始
	 */
	public static void catchFrameFromGif(String gifImgPath, String toImg, int index){
		if(index < 0){
			index = 0;
		}
		int frames = getFrameNumForImg(gifImgPath);
		if(index > frames){
			index = frames - 1;
		}
		String cmdLineStr = CONVERT_CMD_PATH + " " + gifImgPath + "[" + index + "]" + " " + toImg;
		
		try{
			CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("FRAME_ERR", "exec " + cmdLineStr, e);
		}
	}
	
	/**
	 * 
	 * @param gifImgPath gif图片路径
	 * @param index 指定帧的索引，从0开始
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String catchFrameFromGif(String gifImgPath, int index){
		String filename = FilenameUtils.getName(gifImgPath);
		try {
			String baseDir = FilenameUtils.getFullPath(gifImgPath);

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + COMPRESSION_IDENTITY + "." + ext;

			String filePath = baseDir + filename;

			catchFrameFromGif(gifImgPath, filePath, index);

		} catch (Exception e) {
			LOG.error("fromImg:" + gifImgPath + ",index:" + index, e);

			return FilenameUtils.getName(gifImgPath);
		}

		return filename; 
	}
	
	/**
	 * 对图片进行裁切
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param toImg
	 *            被裁切后的图片路径
	 * @param x
	 *            相对图片左上角的x坐标
	 * @param y
	 *            相对图片左上角的y坐标
	 * @param w
	 *            裁切宽度
	 * @param h
	 *            裁切高度
	 * @throws ImgException
	 *             压缩不成功，可能会跑出该异常
	 */
	public static void cutImg(String fromImg, String toImg, int x, int y,
			int w, int h) {
		String repage = " ";
		if("gif".equalsIgnoreCase(FilenameUtils.getExtension(fromImg))){
			repage = " +repage "; //该参数的作用是清空gif图片以外的空白部分
		}
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg + " -crop " + w + "x"
				+ h + "+" + x + "+" + y + repage + toImg;

		try{
		    CmdExecUtil.execCmd(cmdLineStr);
		}catch(Exception e){
			throw new BaseRuntimeException("CUT_ERR", "exec " + cmdLineStr, e);
		}
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param x
	 *            相对图片左上角的x坐标
	 * @param y
	 *            相对图片左上角的y坐标
	 * @param w
	 *            裁切宽度
	 * @param h
	 *            裁切高度
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String cutImg(String fromImg, int x, int y, int w, int h) {
		final String size = w + W_H_SPLITER + h;
		String filename = FilenameUtils.getName(fromImg);
		try {
			String baseDir = FilenameUtils.getFullPath(fromImg);

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + size + "." + ext;

			String filePath = baseDir + filename;

			cutImg(fromImg, filePath, x, y, w, h);

		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",x:" + x + ",y:" + y + ",w:"
					+ w + ",h:" + h,e);

			return FilenameUtils.getName(fromImg);
		}

		return filename; // 压缩成功，则将resultDir+"/"+文件名返回
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param x
	 *            相对图片左上角的x坐标
	 * @param y
	 *            相对图片左上角的y坐标
	 * @param leftx
	 *            相对图片左上角的另外一个x坐标
	 * @param topy
	 *            相对图片左上角的另外一个y坐标
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String cutImgByPx(String fromImg, double x, double y,
			double leftx, double topy) {
		int width = (int) Math.abs(leftx - x);
		int height = (int) Math.abs(topy - y);

		return cutImg(fromImg, (int) x, (int) y, width, height);
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param toImg
	 *            被裁切后的图片路径
	 * @param x
	 *            相对图片左上角的x坐标
	 * @param y
	 *            相对图片左上角的y坐标
	 * @param leftx
	 *            相对图片左上角的另外一个x坐标
	 * @param topy
	 *            相对图片左上角的另外一个y坐标
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static boolean cutImgByPx(String fromImg, String toImg, double x,
			double y, double leftx, double topy) {
		int width = (int) Math.abs(leftx - x);
		int height = (int) Math.abs(topy - y);

		try {
			cutImg(fromImg, toImg, (int) x, (int) y, width, height);
		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",toImg:" + toImg, e);
		}

		return new File(toImg).exists();
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param width
	 *            压缩后的宽度
	 * @param height
	 *            压缩后的高度
	 * @param quality
	 *            压缩质量，从0到100之间的数字
	 * @return 如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String convertUploadImg(String fromImg, int width,
			int height, int quality) {
		final String size = width + W_H_SPLITER + height;

		String baseDir = FilenameUtils.getFullPath(fromImg);
		String filename = FilenameUtils.getName(fromImg);
		String ext = FilenameUtils.getExtension(filename);
		filename = filename + size + "." + ext;

		String filePath = baseDir + filename;
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromImg + " -resize " + width
				+ "x" + height + " -compress JPEG -quality " + quality + " "
				+ filePath;

		try {
			CmdExecUtil.execCmd(cmdLineStr);
		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",width:" + width + ",height:"
					+ height + ",quality:" + quality, e);

			return FilenameUtils.getName(fromImg);
		}

		return filename;
	}

	/**
	 * 
	 * @param fromImg
	 *            原图路径
	 * @param width
	 *            被缩放的宽度
	 * @param height
	 *            被缩放的高度
	 * @return 按指定的宽度或高度，计算出压缩比后进行压缩。如果压缩成功，则返回压缩后的图片名称；如果压缩失败，则返回原图的文件名
	 */
	public static String ratioScaleImg(String fromImg, int width, int height) {
		String filename = FilenameUtils.getName(fromImg);
		try {
			Dimension dim = getDimension(fromImg);

			int accessSize = width;
			double widthPx = dim.getWidth();
			double heightPx = dim.getHeight();
			if (width > 0) {
				height = (int) (heightPx * (width / widthPx));
			} else if (height > 0) {
				accessSize = height;
				width = (int) (widthPx * (height / heightPx));
			} else {
				LOG.warn("one of width or height should be over zero.",
						"fromImg:" + fromImg + ",width:" + width + ",height:"
								+ height, null);
				return filename;
			}

			String ext = FilenameUtils.getExtension(filename);
			filename = filename + accessSize + "." + ext;

			String baseDir = FilenameUtils.getFullPath(fromImg);

			scaleImg(fromImg, baseDir + filename, width, height);
		} catch (Exception e) {
			LOG.error("fromImg:" + fromImg + ",width:" + width + ",height:"
					+ height, e);

			return FilenameUtils.getName(fromImg);
		}

		return filename;
	}
	
	/**
	 * 
	 * @param fromGif
	 * @param FrameIndex
	 * @return 返回指定帧图片的完整路径
	 */
	public static String renameGifByFrameIndex(String fromGif, int frameIndex){
		int frameNum = getFrameNumForImg(fromGif);
		if(frameIndex < 0){
			frameIndex = 0;
		}else if(frameIndex >= frameNum){
			frameIndex = frameNum - 1;
		}
		String newName = FilenameUtils.getFullPath(fromGif) + FilenameUtils.getBaseName(fromGif) + GIF_TMP_FLAG + "." + FilenameUtils.getExtension(fromGif);
		String cmdLineStr = CONVERT_CMD_PATH + " " + fromGif + "["+frameIndex+"] " + newName;
		
		try {
			CmdExecUtil.execCmd(cmdLineStr);
		} catch (Exception e) {
			LOG.error("fromGif:" + fromGif + ",frameIndex:" + frameIndex, e);

			return fromGif;
		}
		
		return newName;
	}

	public static Dimension getDimension(String fromImg) {
		String cmdLineStr = "/usr/bin/identify "+fromImg;

		Dimension dim = null;
		String stdout = CmdExecUtil.execCmdForStdout(cmdLineStr);
		if (StringUtils.isNotEmpty(stdout) && stdout.startsWith(fromImg)) {
			String parts[] = stdout.split("\\s+");
			String dimStr = parts[2];

			String dimParts[] = dimStr.split("x");
			dim = new Dimension(Integer.valueOf(dimParts[0]),
					Integer.valueOf(dimParts[1]));
		}

		return dim;
	}
	
	
	/**
	 * 主要用于gif动态图片，获取gif图片的帧数
	 * @param fromImg
	 * @return
	 */
	public static int getFrameNumForImg(String fromImg){
		String cmdLineStr = "/usr/bin/identify -format '%n' " + fromImg ;
		
		String stdout = CmdExecUtil.execCmdForStdout(cmdLineStr);
		if(StringUtils.isNotBlank(stdout)){
			return Integer.valueOf(stdout.trim());
		}
		
		return new File(fromImg).exists() ? 1 : 0;
	}

	/**
	 * 
	 * @param mapObj
	 *            图片的字节码数组
	 * @return 如果该字节码数组确实为图片格式，则返回true；否则返回false
	 */
	public static boolean isImageType(byte[] mapObj) {
		boolean ret = false;
		ByteArrayInputStream bais = null;
		MemoryCacheImageInputStream mcis = null;
		try {
			bais = new ByteArrayInputStream(mapObj);
			mcis = new MemoryCacheImageInputStream(bais);
			Iterator<ImageReader> itr = ImageIO.getImageReaders(mcis);

			while (itr.hasNext()) {
				ImageReader reader = (ImageReader) itr.next();

				String imageName = reader.getClass().getSimpleName();

				if (imageName != null
						&& ("GIFImageReader".equals(imageName)
								|| "JPEGImageReader".equals(imageName)
								|| "PNGImageReader".equals(imageName) || "BMPImageReader"
									.equals(imageName))) {
					ret = true;
				}
			}
		} finally {
			// 关闭流
			if (mcis != null) {
				try {
					mcis.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
			}

			if (bais != null) {
				try {
					bais.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}

		return ret;
	}

	/**
	 * 
	 * @param file
	 *            文件路径
	 * @return 如果该文件确实为图片格式，则返回true；否则返回false
	 * @throws IOException
	 */
	public static boolean isImageType(String fileName) {
		return isImageType(new File(fileName));
	}

	/**
	 * 
	 * @param file
	 *            文件对象
	 * @return 如果该文件确实为图片格式，则返回true；否则返回false
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean isImageType(File file) {
		try {
			return isImageType(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			LOG.error(file == null ? null : file.getAbsolutePath(),e);
		}

		return false;
	}

	/**
	 * 
	 * @param input
	 *            文件流
	 * @return 如果该文件流确实为图片格式，则返回true；否则返回false
	 * @throws IOException
	 */
	public static boolean isImageType(InputStream input) {
		try {
			return isImageType(IOUtils.toByteArray(input));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}

		return false;
	}

//	public static void main(String[] args) {
//		String method = args[0];
//		String filename = null;
//		if ("scaleImg".equals(method)) {
//			filename = ImageShellUtil.scaleImg(args[1], 70,
//					70);
//		} else if ("compressImg".equals(args[0])) {
//			filename=ImageShellUtil.compressionImg(args[1], 50);
//		} else if ("cutImg".equals(args[0])){
//			filename=ImageShellUtil.cutImg(args[1], 50, 50, 50, 50);
//		} else if("ratioScaleImg".equals(args[0])){
//			filename=ImageShellUtil.ratioScaleImg(args[1], 125, 0);
//		} else if("getDimension".equals(args[0])){
//			try {
//				Dimension dim = ImageShellUtil.getDimension(args[1]);
//				System.out.println("w:" + dim.getWidth() + ",h:" + dim.getHeight());
//			} catch (MagickException e) {
//				e.printStackTrace();
//			}
//		}else if("convertUploadImg".equals(args[0])){
//			filename = ImageShellUtil.convertUploadImg(args[1], 68, 72, 60);
//		}
//		
//		System.out.println("convert filename:" + filename);
//
//	}

}
