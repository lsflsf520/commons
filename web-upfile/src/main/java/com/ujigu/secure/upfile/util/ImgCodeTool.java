package com.ujigu.secure.upfile.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;

public class ImgCodeTool {

	private BufferedImage image;

	/**
	 * createImage : out dest path for image
	 * 
	 * @param fileLocation
	 *            dest path
	 */
	private void createImage(String fileLocation) {
		try {
			FileOutputStream fos = new FileOutputStream(fileLocation);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ImageIO.write(image, "png", bos);
			// com.sun.image.codec.jpeg.JPEGImageEncoder encoder =
			// com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(bos);
			// encoder.encode(image);
			bos.flush();
			bos.close();
			LogUtils.debug("@2 %s 图片生成输出成功", fileLocation);
		} catch (Exception e) {
			LogUtils.warn("@2 %s 图片生成输出失败", fileLocation);
		}
	}

	/**
	 * createFileIconImage ：create share file list icon
	 * 
	 * @param destOutPath
	 *            create file icon save dictory
	 */
	public void createFileIconImage(String destOutPath) {
		// get properties operate tool
		// get share file root path
		String shareFileRootPath = BaseConfig.getValue("FileShareRootPath");
		// root dictory
		File rootDictory = new File(shareFileRootPath);
		// child file list
		File[] fileList = rootDictory.listFiles();
		// child list files
		File file = null;
		if (fileList != null && fileList.length > 0) {
			LogUtils.info("分享文件根目录下文件数:%d" + fileList.length);
			for (int i = 0, j = fileList.length; i < j; i++) {
				String fileName = fileList[i].getName();
				String fileAllName = shareFileRootPath + fileName;
				file = new File(fileAllName);
				// get file system icon
				ImageIcon fileIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
				image = (BufferedImage) fileIcon.getImage();
				if (image != null) {
					LogUtils.debug("@1 %s 文件的图标获取成功", fileName);
				}
				Graphics g = image.getGraphics();
				g.drawImage(image, 0, 0, null);
				String fileNameX = fileName.substring(0, fileName.lastIndexOf("."));
				// out image to dest
				createImage(destOutPath + "\\" + fileNameX + ".png");
				LogUtils.debug("@3 %s 文件的图标生成成功", fileName);
			}
		}
	}

	/**
	 * creatDefaultVerificationCode ：create default verification code
	 * 
	 * @param destOutPath
	 *            create creatDefaultVerificationCode save dictory
	 */
	public void creatDefaultVerificationCode(String destOutPath, int width, int height) {
		// verification code image height
		// comment to com.tss.fileshare.tools.VerificationCodeTool 65 row,please
//		int disturblinesize = 15;
		VerifyCodeTool vcTool = new VerifyCodeTool();
		// default verification code
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(vcTool.getRandomColor(200, 250));
		g.drawRect(0, 0, width - 2, height - 2);
		for (int i = 0; i < VerifyCodeTool.DISTURB_LINE_SIZE; i++) {
			vcTool.drawDisturbLine1(g, width, height);
			vcTool.drawDisturbLine2(g, width, height);
		}
		// 玩命加载中…
		String defaultVCString = "\u73A9\u547D\u52A0\u8F7D\u4E2D\u2026";
		String dfch = null;
		for (int i = 0; i < 6; i++) {
			dfch = String.valueOf(defaultVCString.charAt(i));
			vcTool.drawRandomString((Graphics2D) g, dfch, i);
		}
		LogUtils.debug("默然验证码生成成功");
		// Graphics gvc = imagevc.getGraphics();
		createImage(destOutPath + "\\defaultverificationcode.jpeg");
	}

	/**
	 * graphicsGeneration : create image
	 * 
	 * @param imgurl
	 *            display picture url . eg:F:/imagetool/7.jpg<br/>
	 * @param imageOutPathName
	 *            image out path+naem .eg:F:\\imagetool\\drawimage.jpg<br/>
	 * @param variableParmeterLength
	 *            ; int, third parameter length.<br/>
	 * @param drawString
	 *            variableParmeterLength ;String [] .<br/>
	 */
	public void graphicsGeneration(String imgurl, String imageOutPathName, int variableParmeterLength,
			String... drawString) {
		// The width of the picture
		int imageWidth = 500;
		// The height of the picture
		int imageHeight = 400;
		image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.WHITE);
		// drawing image
		graphics.fillRect(0, 0, imageWidth, imageHeight);
		graphics.setColor(Color.BLACK);
		// draw string string , left margin,top margin
		for (int i = 0, j = variableParmeterLength; i < j; i++) {
			graphics.drawString(drawString[i], 50, 10 + 15 * i);
		}
		// draw image url
		ImageIcon imageIcon = new ImageIcon(imgurl);
		// draw image , left margin,top margin
		// The coordinates of the top left corner as the center(x,y)[left top
		// angle]
		// Image observer :If img is null, this method does not perform any
		// action
		graphics.drawImage(imageIcon.getImage(), 200, 0, null);
		createImage(imageOutPathName);
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

}
