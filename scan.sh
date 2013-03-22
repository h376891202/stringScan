#excute path. the root director of the project
#author yadong.huang
#time: 12/11/02

#This is the diff out log dir
OUTDIR="stringout"
FILE_EXCUTE_LOG="java_excute_log"
CURRENT_DIR=`pwd`
SCAN_XML_JAVA_FILE="GetNoTranslate"
CREATE_XLS_LOG="create_xls_log"
READ_XLS_LOG="read_xls_log"
cd ..
PROJECT_ROOT_DIR=`pwd`
cd $CURRENT_DIR

function clear(){
	rm -fr $OUTDIR 2> /dev/null
	rm $CURRENT_DIR/$FILE_EXCUTE_LOG.log 2> /dev/null
	rm $CURRENT_DIR/$FILE_EXCUTE_LOG.log_err 2> /dev/null
	rm $CREATE_XLS_LOG 2> /dev/null
	rm $READ_XLS_LOG 2> /dev/null
	rm -fr xmlout 2> /dev/null
	rm translate.xls 2> /dev/null
	rm *.class 2> /dev/null
	rm errorfile 2> /dev/null
}
#Programe start.....
if [ "$1" == "clean" ];then
	clear

elif [ "$1" == "scan" ];then
	clear
	mkdir $OUTDIR
	test -e $SCAN_XML_JAVA_FILE.class
	if [ $? != 0 ];then 
		javac -verbose -classpath dom4j-1.6.1.jar GetNoTranslate.java
	fi

	test -e CreateXLS.class
	if [ $? != 0 ];then
		javac -verbose -classpath jxl.jar CreateXLS.java
	fi	

	cd $PROJECT_ROOT_DIR
	valuesdir=$(find -name "values" -type d | grep -v "^.\/device" | grep -v "^./cts"| grep -v "^.\/development" | grep -v "^./\external" |grep -v "test")

	for var in $valuesdir
		do
		cd $PROJECT_ROOT_DIR
		cd $var/../
		fileName=$(echo $var | tr "[:punct:]" "_")
		cp $CURRENT_DIR/$SCAN_XML_JAVA_FILE.class .
		pwd
		if [ "$2" == "" ];then
			java -cp $CURRENT_DIR/dom4j-1.6.1.jar:. $SCAN_XML_JAVA_FILE $fileName >> $CURRENT_DIR/$FILE_EXCUTE_LOG.log 
		else
			java -cp $CURRENT_DIR/dom4j-1.6.1.jar:. $SCAN_XML_JAVA_FILE $fileName $2  >> $CURRENT_DIR/$FILE_EXCUTE_LOG.log
		fi
		rm $SCAN_XML_JAVA_FILE.class
		mv $fileName $CURRENT_DIR/$OUTDIR
	done

	echo "scan file done , pls check the java runtime log " $CURRENT_DIR/$FILE_EXCUTE_LOG.log 
	cd $CURRENT_DIR
	java -cp jxl.jar:. CreateXLS > $CREATE_XLS_LOG
	echo "create xls done , pls check the log " $CURRENT_DIR/$CREATE_XLS_LOG
elif [ "$1" == "write" ];then
	test -e ReadXLS.class
	if [ $? != 0 ];then
		javac -verbose -classpath jxl.jar ReadXLS.java
	fi
	java -cp jxl.jar:. ReadXLS  > $READ_XLS_LOG
	echo "create xml success , pls check the log " $CURRENT_DIR/$READ_XLS_LOG
else
	cat README
fi

