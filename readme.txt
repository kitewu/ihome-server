ihome服务器程序说明：
1、开发环境：
	windows10_64
	eclipse-jee-neon-R-win32-x86_64
	jdk1.8.0_102
2、测试运行环境
	centos6.5_64  Linux wslzjy.cn 2.6.32-504.30.3.el6.x86_64
	openjdk version "1.8.0_71"
	mysql  Ver 14.14 Distrib 5.1.73, for redhat-linux-gnu (x86_64)
3、执行文件
	ihome_server.jar
	NLPIR/
		Data/
		mykey.txt
	linux-x86-64/
		libNLPIR.so
	lib/
		linux32/
		linxu64/
		win32/
		win64/
	注：1.开发时引用jar包，已包含在ihome_server.jar中。
		2.NLPIR/和linux-x86-64/目录与分此功能有关，运行时必须包含两个目录中的全部内容。
		3.mykey.txt中存放着分词关键字，可更改。
		4.Data中包含NLPIR.user license，过期后去https://github.com/NLPIR-team/NLPIR/tree/master/License/license%20for%20a%20month/NLPIR-ICTCLAS%E5%88%86%E8%AF%8D%E6%8E%88%E6%9D%83下载并替换。
		5.链接库文件的引用与系统相关，
			linux64位系统下，将lib/linux32目录下的文件复制到linux-x86-64目录下
			linux32位系统下，将lib/linux32目录下的文件复制到linux-x86目录下
			windows系统则将lib/win32or64下的dll文件复制到项目根目录下即可
4、ServerSocket端口号：5678











