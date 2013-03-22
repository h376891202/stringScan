
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * Read xls and write it to a strings.xml
 * @author Administrator
 *
 */

public class ReadXLS {
	private static final int LINE_NAME=0;
	private static final int LINE_TRASLATEBLE=2;
	private static final int LINE_TARGET_STRING = 3;
	private static final String NEEDTRANSLATE = "yes";
	private static final String NEEDNOTTRANSLATE = "no";
	private static final String XLS_FILE_NAME = "translate.xls";
	private static final String HEAD = "<string name=\"";
	private static final String CENTER = "\">";
	private static final String FOOT = "</string>";
	private static final String OUT_DIR = "xmlout";
	private static final int START_LINE = 2;
	
	private Workbook book;
	
	private ReadXLS() throws BiffException, IOException{
		File xlsFile = new File(XLS_FILE_NAME);
		if(!xlsFile.exists()){
			logE("xls file " + XLS_FILE_NAME + "does not exsist.!");
			System.exit(0);
		}
		WorkbookSettings workbookSettings = new WorkbookSettings();
                workbookSettings.setEncoding("ISO-8859-1");
		FileInputStream fileInputStream = new FileInputStream(xlsFile);
		book = Workbook.getWorkbook(fileInputStream,workbookSettings);
	}
	
	private void readSheet(){
		Sheet sheets[] = book.getSheets();
		//create root dir at first
		if(!createRootDir()){
			return;
		}
		for(int page = 0;page < sheets.length;page++){
			Sheet sheet = sheets[page];
			wirteToXml(sheet);
		}
		book.close();
	}
	
	private void wirteToXml(Sheet sheet){
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			String fileName = null;
			try {
				fileName = sheet.getCell(0,0).getContents();
			} catch (ArrayIndexOutOfBoundsException e1) {
				logE("sheet "+sheet.getName()+" format error skip it");
			}
			File file = new File(OUT_DIR+"/"+fileName);
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			int cloumes = sheet.getRows();
			logV("writeFile "+file+"start  <<<<<<<<<<<<<<<<<<<<<<<<");
			for(int row = START_LINE;row < cloumes;row++){
				String name = sheet.getCell(LINE_NAME, row).getContents();
				String traslateble = sheet.getCell(LINE_TRASLATEBLE, row).getContents();
				try {
					if(NEEDTRANSLATE.equals(traslateble)){
						String target = sheet.getCell(LINE_TARGET_STRING, row).getContents();
						String value = buildString(name,traslateble,target);
						if(value != null){
							logV("file " + file + " value " + value);
							writer.write(value+"\n");
							writer.flush();
						}
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					logE(name + "have no translate!");
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logE(e.getMessage());
		}finally{
			try {
				fileWriter.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				logE(e.getMessage());
			}
		}
	}
	private boolean createRootDir(){
		File out_dir = new File(OUT_DIR);
		if(out_dir.exists()){
			File files [] = out_dir.listFiles();
			for(File file:files){
				file.delete();
			}
			boolean removeSuccess = out_dir.delete();
			if(!removeSuccess){
				logE("remove root dir " + OUT_DIR + "error pls remove " + OUT_DIR + " by yourself");
				return false;
			}
		}
		return out_dir.mkdir();

	}
	private String buildString(String name, String traslateble, String target) {
		if (NEEDNOTTRANSLATE.equals(traslateble))
			return null;
		if (null == target){
			logE(name+"have not translate!");
			return null;
		}
		if (target.isEmpty()){
			logW(name+"have not traslate , values is a null string");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(HEAD);
		sb.append(name);
		sb.append(CENTER);
		sb.append(target);
		sb.append(FOOT);
		return sb.toString();
	}
	
	public static void main(String args []){
		try {
			new ReadXLS().readSheet();
		} catch (BiffException e) {
			e.printStackTrace();
			logE(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logE(e.getMessage());
		}
	}
	
	private static void logE(String message){
		System.out.println("ERROR : "+message);
	}
	
	private static void logV(String message){
		System.out.println("DEBUG : "+message);
	}
	
	private static void logW(String message){
		System.out.println("WRAING : "+message);
	}

}

