package core;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import util.ExcelManager;

import java.io.File;
import java.util.Map;

public class MainController {
    @FXML public Pane mainPane;

    @FXML public TextArea txtAreaLog;
    @FXML public Button btnStockListChooser;
    @FXML public Button btnOriginChooser;
    @FXML public TextField txtStockFilePath;
    @FXML public TextField txtOriginFilePath;
    @FXML public Button btnConfirm;
    @FXML public Button btnCodeGeneration;
    @FXML public ProgressBar progressbar;
    @FXML public Label progressText;

    public void initialize() {
        FileChooser fileChooser = new FileChooser();
        DirectoryChooser directoryChooser = new DirectoryChooser();

        ExcelManager excelManager = new ExcelManager(txtAreaLog, progressbar, progressText);

        btnStockListChooser.setOnMouseClicked(event -> {
            addLog("제품리스트 선택 중...");
            // 파일선택
            fileChooser.setTitle("제품리스트 선택");
            File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());

            // 결과에 따른 로그
            if (file == null)
                addLog("파일 선택이 취소됨.");
            else {
                addLog("파일 선택 : [" + file.getName() + "]");
                txtStockFilePath.setText(file.getAbsolutePath());
            }
        });

        btnOriginChooser.setOnMouseClicked(event -> {
            addLog("재고리스트 선택 중...");
            // 파일선택
            fileChooser.setTitle("재고리스트 선택");
            File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());

            // 결과에 따른 로그
            if (file == null)
                addLog("파일 선택이 취소됨.");
            else {
                addLog("파일 선택 : [" + file.getName() + "]");
                txtOriginFilePath.setText(file.getAbsolutePath());
            }
        });

        btnCodeGeneration.setOnMouseClicked(event -> {
            addLog("코드생성 결과물 저장 위치 선택 중...");
            // 파일이 없을시 취소
            if (!validate()) return;

            String origin = txtOriginFilePath.getText();
            String target = txtStockFilePath.getText();

            // 파일선택
            fileChooser.setTitle("생성 결과물 위치");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)","*.xlsx"));
            File fileToSave = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            // 결과에 따른 로그
            if (fileToSave == null) {
                addLog("위치 선택이 취소됨.");
                return;
            }

            if (!fileToSave.getName().contains("."))
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");

            addLog("위치 선택 : [" + fileToSave.getName() + "]");

            addLog("원본파일로부터 코드 추출 시작...");

            File finalFileToSave = fileToSave;
            Thread thread = new Thread(() -> {
                try {
                    Map<String, String> nameToCode = excelManager.getCodeMapCodeAsValue(origin);
                    excelManager.writeCodeFile(nameToCode, target, finalFileToSave.getAbsolutePath());
                    addLog("코드 추출 완료");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            thread.setDaemon(true);
            thread.start();
        });

        btnConfirm.setOnMouseClicked(event -> {
            addLog("반영재고 결과물 저장 위치 선택 중...");

            // 파일이 없을시 취소
            if (!validate()) return;

            String origin = txtOriginFilePath.getText();
            String target = txtStockFilePath.getText();

            // 파일선택
            directoryChooser.setTitle("생성 결과물 위치");
            File directoryToSave = directoryChooser.showDialog(mainPane.getScene().getWindow());

            // 결과에 따른 로그
            if (directoryToSave == null) {
                addLog("위치 선택이 취소됨.");
                return;
            }

            addLog("위치 선택 : [" + directoryToSave.getName() + "]");

            addLog("원본파일로부터 재고수량 검토 시작...");

            Thread thread = new Thread(() -> {
                try {
                    Map<String, String> codeToQuantity = excelManager.getCodeMapCodeAsKey(origin);
                    excelManager.writeFullFiles(codeToQuantity, target, directoryToSave.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            thread.setDaemon(true);
            thread.start();
        });
    }

    @FXML public void addLog(String log) {
        txtAreaLog.appendText("\t" + log + "\n");
    }

    public boolean validate() {
        // 파일 확인
        if (txtStockFilePath.getText() == null || txtStockFilePath.getText().trim().isEmpty()) {
            addLog("쇼핑몰재고 파일이 없습니다.");
            return false;
        } else if (txtOriginFilePath.getText() == null || txtOriginFilePath.getText().trim().isEmpty()) {
            addLog("재고현황원본 파일이 없습니다.");
            return false;
        }

        return true;
    }
}