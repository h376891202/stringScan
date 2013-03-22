
/*

 * Created on 2012/11/1

 */


import java.io.*;

import jxl.*;

import jxl.write.*;
import jxl.write.biff.RowsExceededException;


public class CreateXLS {
	private static final int LINE_NAME=0;
	private static final int LINE_EN_STRING=1;
	private static final int LINE_TRASLATEBLE=2;
	private static final int LINE_TARGET_STRING=3;
	private static final String SEPARATOR = ":";
	private static final String NEEDTRANSLATE = "yes";
	private static final String NEEDNOTTRANSLATE = "no";
	private static final String XLS_FILE_NAME = "translate.xls";
	private static final int START_ROW = 2;
	private static final String TITLE_NAME = "name";
	private static final String TILTE_ENGLISH = "english";
	private static final String TITLE_TRANSLATE = "need_translate";
	private static final String TITLE_TARGET = "translate";
	
	private int currentRow = START_ROW;
	private int currentSheet = 0;
	private WritableWorkbook book;
	private WritableSheet sheet;
	private StringBuilder sb;
	
	
	private CreateXLS() throws IOException{
		book = Workbook.createWorkbook(new File(XLS_FILE_NAME));
		sb = new StringBuilder();
	}
	
	private void createSheet(){
		currentSheet += 1;
		currentRow = START_ROW;
		sheet = book.createSheet("sheet"+currentSheet, currentSheet++);
	}
	private void writeLine(String name,String en_string,int translateble,String tar_string) throws RowsExceededException, WriteException, IOException{
		logV("writeLine currentSheet="+sheet.getName()+"currentRow="+currentRow+"\n");
		jxl.write.Label nameLabel = new jxl.write.Label(LINE_NAME, currentRow, name);
		jxl.write.Label enStringLabel = new jxl.write.Label(LINE_EN_STRING, currentRow, en_string);
		jxl.write.Label needTranslate = new jxl.write.Label(LINE_TRASLATEBLE, currentRow 
				,translateble==0?NEEDNOTTRANSLATE:NEEDTRANSLATE);
		jxl.write.Label tarStringLabel = new jxl.write.Label(LINE_TARGET_STRING, currentRow, tar_string);
		sheet.addCell(nameLabel);
		sheet.addCell(enStringLabel);
		sheet.addCell(needTranslate);
		sheet.addCell(tarStringLabel);
		currentRow++;
	}
	private void close() throws WriteException, IOException{
		if(book != null){
			book.write();
			book.close();
		}
	}
	private void createTitle(String fileName) throws RowsExceededException, WriteException{
		//write title
		WritableFont font=new WritableFont(WritableFont.createFont("楷体_GB2312"),24,WritableFont.BOLD);
		WritableCellFormat titleFont=new WritableCellFormat(font);
		jxl.write.Label titleLabel = new jxl.write.Label(0, 0, fileName,titleFont);
		//merge cell
		sheet.mergeCells(0,0,10,0);
		sheet.addCell(titleLabel);
		//write colume name
		WritableFont nameFont=new WritableFont(WritableFont.createFont("楷体_GB2312"),22,WritableFont.NO_BOLD);
		WritableCellFormat nameFormat=new WritableCellFormat(nameFont);
		jxl.write.Label nameLabel1 = new jxl.write.Label(0, 1, TITLE_NAME,nameFormat);
		jxl.write.Label nameLabel2 = new jxl.write.Label(1, 1, TILTE_ENGLISH,nameFormat);
		jxl.write.Label nameLabel3 = new jxl.write.Label(2, 1, TITLE_TRANSLATE,nameFormat);
		jxl.write.Label nameLabel4 = new jxl.write.Label(3, 1, TITLE_TARGET,nameFormat);
		sheet.addCell(nameLabel1);
		sheet.addCell(nameLabel2);
		sheet.addCell(nameLabel3);
		sheet.addCell(nameLabel4);
	}
	
	private void readAndWriteFile(File file){
		BufferedReader reader = null;
		FileReader fileReader = null;
		try {
			createSheet();
			createTitle(file.getName());
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			boolean removeSheetForNull = true;
			while(reader.ready()){
				removeSheetForNull = false;
				String content = reader.readLine();
				String [] values = content.split(":");
				try {
					int needTranslate = Integer.parseInt(values[2]);
					if(needTranslate != 1 && needTranslate != 0){
						logE("file "+file+" needtraslate must be 1 or 0");
						continue;
					}
					writeLine(values[0], values[1], needTranslate,values[3]);
				} catch (NumberFormatException e) {
					logE("file "+file+" number formate error");
					sb.append(content+"\n");
					continue;
				} catch (ArrayIndexOutOfBoundsException a){
					logE("file "+file+" ArrayIndexOutOfBoundsException");
					sb.append(content+"\n");
					continue;
				}
			}
			//if the sheet is null remove it 
			if(removeSheetForNull){
				logV(file.getName()+" is null remove the sheet");
				book.removeSheet(currentSheet);
			}
		writeToFile("errorfile",sb.toString());
		} catch (RowsExceededException e) {
			e.printStackTrace();
			logE(e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logE(e.getMessage());
		} catch (WriteException e) {
			e.printStackTrace();
			logE(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logE(e.getMessage());
		} finally{
			try {
				if(null != fileReader)
					fileReader.close();
				if(null != reader)
					reader.close();
			} catch (IOException e) {
				logE(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	private void writeToFile(String fileName,String s){
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			File file = new File(fileName);
			fileWriter = new FileWriter(file,true);
			writer = new BufferedWriter(fileWriter);
			writer.append(s.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				writer.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void readFileAndWrite() throws WriteException, IOException {
		File directory = new File("stringout");
		if(directory.exists() && directory.isDirectory()){
			File [] listFile = directory.listFiles();
			for(File file : listFile){
				readAndWriteFile(file);
			}
			close();
		}else{
			logE("out director \"stringout\" error");
		}
	}
	
	
	public static void main(String[] args) {
		try {
			new CreateXLS().readFileAndWrite();
		}catch (WriteException e) {
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

	

}

