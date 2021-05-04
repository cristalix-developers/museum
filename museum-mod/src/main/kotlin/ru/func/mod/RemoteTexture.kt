package ru.func.mod

import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

data class RemoteTexture(val RC : ResourceLocation , val address : String, val sha1 : String)

fun loadTexture(images: MutableList<RemoteTexture>){

    val cacheDir = Paths.get("museum/")
    if(!Files.exists(cacheDir))
        Files.createDirectory(cacheDir)
    images.forEach {
        val texture = it
        val path = cacheDir.resolve(texture.sha1)

        val image = try {
            Files.newInputStream(path).use {
                ImageIO.read(it)
            }
        } catch (ex: IOException) {
            try {
                val url = URL(texture.address)
                val image = ImageIO.read(url);
                val baos = ByteArrayOutputStream()
                ImageIO.write(image, "png", baos)
                baos.flush()
                val imageInByte = baos.toByteArray()
                baos.close()
                Files.write((Paths.get(cacheDir.toString() + "/" + texture.sha1)), imageInByte)
                image
            }catch (e:IOException){
                null
            }
        }
        UIEngine.clientApi.renderEngine().loadTexture(it.RC, UIEngine.clientApi.renderEngine().newImageTexture(image!!,false,false))
    }
}
