import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class GetNoTranslate {
	private static final String EN_FILE = "values/strings.xml";
	private static final String DEF_FILE = "values-es/strings.xml";
	private static final String FILE_NAME = "/strings.xml";
	private static final String VALUE = "values-";
	private static final String SEPARATOR = ":";
	
	private Map<String,String> mMaps;
	private StringBuilder mWriteToFile;
	private boolean mSaveAll = false;
	private File mEnFile = null;
	private File mTargetFile = null;
	
	private GetNoTranslate(){
		mMaps = new HashMap<String,String>();
		mWriteToFile = new StringBuilder();
	}
	
	private boolean initFile(String targetFileName){
		mEnFile = new File(EN_FILE);
		File target_file = null;
		if(targetFileName == null || targetFileName.isEmpty()){
			target_file = new File(DEF_FILE);
		}else{
			target_file = new File(VALUE+targetFileName+FILE_NAME);
		}
		if(!mEnFile.exists()){
			logE("en file "+mEnFile+"does not exit , skip this directory");
			return false;
		}
		if(null == target_file || !target_file.exists()){
			mSaveAll = true;
			logW("target file "+target_file+" does not exist,we need save all string!");
		}
		mTargetFile = target_file;
		return true;
	}
	
	private void diffFiles(String targetFileName) throws DocumentException{
		//check file, if en file is not exist exit this programe
		if(!initFile(targetFileName)){
			return;
		}
		SAXReader reader = new SAXReader();  
		Document document = null;
		Iterator<Element> it = null;
		if (!mSaveAll) {
			document = reader.read(mTargetFile);
			it = getIterator(document);
			// setup 1 get target file ,and put them to a Map
			while (it.hasNext()) {
				Element elm = (Element) it.next();
				mMaps.put(elm.attributeValue("name"), (String) elm.getData());
			}
			//setup 2 read the en file , and make diff
			document = reader.read(mEnFile);
			it = getIterator(document);
			while (it.hasNext()) {
				Element elm = (Element) it.next(); 
				appendToStringBuff(elm);
			}
		} else {
			try {
				//save all
				document = reader.read(mEnFile);
				it = getIterator(document);
				while (it.hasNext()) {
					Element elm = (Element) it.next(); 
					appendToStringBuff(elm);
				}
			} catch (Exception e) {
				logE(e.getMessage());
				e.printStackTrace();
			}
		}
		//save to file
	}
	
	public void appendToStringBuff(Element elm){
		String name= elm.attributeValue("name");
		String data = (String)elm.getData();
		String traslate = elm.attributeValue("translatable");
		data = data.replace("\n","");
		data = data.replace("\""," ");
		//if the name is not exist in mMaps , put it to writeTofile
		String tarString = mMaps.get(name);
		mWriteToFile.append(name + SEPARATOR);
		mWriteToFile.append(data + SEPARATOR);
		System.out.println("yadong traslate="+traslate);
		if(null != traslate && "false".equals(traslate)){
			mWriteToFile.append("0" + SEPARATOR);
		} else {
			mWriteToFile.append("1" + SEPARATOR);
		}
		mWriteToFile.append(tarString+"\n");
		logV("save colume:"+name+":"+data+":"+"traslate");
	}
	
	private void writeToFile(String fileName){
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			File file = new File(fileName);
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			writer.write(mWriteToFile.toString());
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
	private Iterator<Element> getIterator(Document document){
		Element rootElm = document.getRootElement();  
		List nodes = rootElm.elements("string");
		Iterator it = nodes.iterator();
		return it;
	}
	
	public static void main(String args []){
		try {
			GetNoTranslate getNoTranslate = new GetNoTranslate();
			String fileName = args[0];
			if(null == fileName || fileName.isEmpty()){
				logE("frist parameter must a fileName can't be empty");
				return;
			}
			logV("start write to " + fileName);
			String targetFile = null;
			try {
				targetFile = args[1];
			} catch (ArrayIndexOutOfBoundsException e) {}
			getNoTranslate.diffFiles(targetFile);
			getNoTranslate.writeToFile(fileName);
		} catch (DocumentException e) {
			e.printStackTrace();
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


