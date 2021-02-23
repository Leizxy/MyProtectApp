package cn.leizy.shell

import java.io.File
import java.io.FileOutputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("hello main")

            //初始化相应目录，或删除之前的文件。
            val tempFileApk = File("source/apk/temp")
            if (tempFileApk.exists()) {
                tempFileApk.listFiles()?.forEach {
                    it?.run {
                        if (isFile) {
                            delete()
                        }
                    }
                }
            } else {
                tempFileApk.mkdirs()
            }
            val tempFileAar = File("source/aar/temp")
            if (tempFileAar.exists()) {
                tempFileAar.listFiles()?.forEach {
                    it?.run {
                        if (isFile) {
                            delete()
                        }
                    }
                }
            } else {
                tempFileAar.mkdirs()
            }

            /**
             * 1.处理原始apk 加密dex
             */
            AES.init(AES.DEFAULT_PWD)
            //解压apk
            val apkFile = File("source/apk/app-debug.apk")
            val newApkFile = File(apkFile.parent + File.separator + "temp")
            if (!newApkFile.exists()) newApkFile.mkdirs()
            val mainDexFile = AES.encryptAPKFile(apkFile, newApkFile)
            if (newApkFile.isDirectory) {
                newApkFile.listFiles()?.forEach {
                    if (it.isFile) {
                        if (it.name.endsWith(".dex")) {
                            val name = it.name
                            println("rename 1:$name")
                            val cursor = name.indexOf(".dex")
                            val newName =
                                it.parent + File.separator + name.substring(0, cursor) + "_.dex"
                            println("rename 2:$newName")
                            it.renameTo(File(newName))
                        }
                    }
                }
            }

            /**
             * 2.处理aar 获得壳dex
             */
            val aarFile = File("source/aar/shelllib-debug.aar")
            val aarDex = Dx.jar2Dex(aarFile)
            val tempMainDex = File(newApkFile.path + File.separator + "classes.dex")
            if (!tempMainDex.exists()) {
                tempMainDex.createNewFile()
            }
            val fos = FileOutputStream(tempMainDex)
            val fbytes = Utils.getBytes(aarDex)
            fos.write(fbytes)
            fos.flush()
            fos.close()

            /**
             * 3.打包签名
             */
            val unsignedApk = File("result/apk-unsigned.apk")
            unsignedApk.parentFile.mkdirs()
            Zip.zip(newApkFile, unsignedApk)
            val signedApk = File("result/apk-signed.apk")
            Signature.signature(unsignedApk, signedApk, "F:\\code\\MyProtectApp\\leizy.jks")
        }
    }
}
//fun main() {
//    println("hello")
//}