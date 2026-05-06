package org.example.tamaapi.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String fileName){
        return fileDir + fileName;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) {
        List<UploadFile> storeFileResult  = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty())
                storeFileResult.add(storeFile(multipartFile));
        }
        return storeFileResult;
    }


    // 똑같은 파일 이름 존재하는지 검증 필요
    public UploadFile storeFile(MultipartFile multipartFile)  {
        if(multipartFile.isEmpty())
            return null;

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFilename);

        //로컬 업로드
        try {
            multipartFile.transferTo(new File(getFullPath(storedFileName)));
        } catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }

        return new UploadFile(originalFilename, storedFileName);
    }

    private String createStoredFileName(String originalFileName){
        String ext = extractExt(originalFileName);
        String name = extractName(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return name + "-" + uuid + "." + ext;
    }

    private String extractExt(String originalFileName){
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }

    private String extractName(String originalFileName){
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(0, pos);
    }

    public void areFilesImage(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            // 파일의 MIME 타입이 이미지인지 확인
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일이 아닙니다");
            }
        }
    }


}
