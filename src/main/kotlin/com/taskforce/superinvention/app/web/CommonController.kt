package com.taskforce.superinvention.app.web

import com.taskforce.superinvention.common.util.aws.s3.S3Path
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartFile

@RestControllerAdvice
class CommonController(
) {

    @PostMapping("/file/temp")
    fun fileTempSave(file: MultipartFile) : S3Path {


        return S3Path()
    }
}