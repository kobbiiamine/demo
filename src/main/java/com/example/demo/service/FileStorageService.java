package com.example.demo.service;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.demo.entity.FileDB;
import com.example.demo.repository.FileDBRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;


@Service
public class FileStorageService {

    @Autowired
    private FileDBRepository fileDBRepository;

    public FileDB store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());

        return fileDBRepository.save(fileDB);
    }

    public FileDB getFile(String id) {
        return fileDBRepository.findById(id).get();
    }

    public Stream<FileDB> getAllFiles() {
        return fileDBRepository.findAll().stream();
    }

    private void retrieveFile(File file, String fileName) throws FileNotFoundException {
        byte[] data = fileDBRepository.findByName(fileName).getData();
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileDB attachImageToFile(int x,int y,int page, String imageName, String pdfName) throws IOException {

        File outputFile = new File("pdfFile.pdf");
        retrieveFile(outputFile,pdfName);

        PDDocument doc = PDDocument.load(outputFile);
        PDPage pagePdf = doc.getPage(page);

        File imageFile = new File("imageFile" +"." + getExtensionByStringHandling(imageName).get());
        retrieveFile(imageFile,imageName);

        FileInputStream bais = new FileInputStream(imageFile);
        BufferedImage bim = ImageIO.read(bais);
        PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bim);
        PDPageContentStream contents = new PDPageContentStream(doc, pagePdf);
        contents.drawImage(pdImage, x, y);
        contents.close();
        //Saving the document
        doc.save("attachedFile.pdf");
        doc.close();

        return saveFileIntoDatabase();

    }

    private FileDB saveFileIntoDatabase() throws IOException {
        File attachedFile = new File("attachedFile.pdf");
        byte[] bytes = Files.readAllBytes(attachedFile.toPath());
        FileDB fileDB = new FileDB("attachedFile.pdf", "pdf",bytes);
        fileDBRepository.save(fileDB);

        return fileDB;
    }


    private Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}