import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class EdgeDetection {
    public static void main(String[] args) {
        try {
            // 1. 讀取輸入影像
            String inputFileName = args.length > 0 ? args[0] : "targer.jpg";
            File inputFile = new File(inputFileName);

            if (!inputFile.exists()) {
                System.out.println("找不到圖片檔案：" + inputFileName);
                System.out.println("請將圖片放在程式目錄中，或傳入正確的檔案路徑。例：java EdgeDetection input.jpg");
                return;
            }

            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                if (isWebP(inputFile)) {
                    System.out.println("檔案看起來是 WebP 格式，但 Java ImageIO 未內建支援 WebP。請先轉換成 JPEG 或 PNG，再執行程式。");
                } else {
                    System.out.println("無法讀取圖片檔案，可能是格式不支援或檔案已損壞：" + inputFileName);
                    System.out.println("請使用 JPEG、PNG 或 BMP 格式的圖片，並確認副檔名與實際格式一致。");
                }
                return;
            }

            System.out.println("成功讀取圖片：" + inputFileName);
            int width = image.getWidth();
            int height = image.getHeight();

            // 建立新的影像來儲存邊緣偵測的結果
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            // 2. 雙層迴圈遍歷每個像素 
            // 從 index 1 開始，避免計算邊緣像素時超出陣列範圍 (Sobel 需要 3x3 範圍)
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {

                    // 取得周圍 3x3 範圍的灰階像素值
                    int topLeft     = getGrayScale(image.getRGB(x - 1, y - 1));
                    int topCenter   = getGrayScale(image.getRGB(x, y - 1));
                    int topRight    = getGrayScale(image.getRGB(x + 1, y - 1));

                    int left        = getGrayScale(image.getRGB(x - 1, y));
                    // 中心點 (x, y) 權重為 0，不需取值
                    int right       = getGrayScale(image.getRGB(x + 1, y));

                    int bottomLeft  = getGrayScale(image.getRGB(x - 1, y + 1));
                    int bottomCenter= getGrayScale(image.getRGB(x, y + 1));
                    int bottomRight = getGrayScale(image.getRGB(x + 1, y + 1));

                    // 3. 套用講義上的 Sobel 算子公式
                    // 水平方向梯度 Ix
                    int Ix = (topRight + 2 * right + bottomRight) - (topLeft + 2 * left + bottomLeft);
                    
                    // 垂直方向梯度 Iy
                    int Iy = (bottomLeft + 2 * bottomCenter + bottomRight) - (topLeft + 2 * topCenter + topRight);

                    // 4. 計算整體的邊緣強度 (Magnitude)
                    int magnitude = (int) Math.sqrt(Ix * Ix + Iy * Iy);

                    // 確保像素數值落在合法的 0~255 範圍內
                    magnitude = Math.min(255, Math.max(0, magnitude));

                    // 5. 將計算出的強度設定為新圖片的像素顏色
                    Color edgeColor = new Color(magnitude, magnitude, magnitude);
                    outputImage.setRGB(x, y, edgeColor.getRGB());
                }
            }

            // 6. 輸出結果影像
            File outputFile = new File("output_edge_sobel.jpg");
            ImageIO.write(outputImage, "jpg", outputFile);
            System.out.println("邊緣偵測完成！已產生 output_edge_sobel.jpg");

        } catch (IOException e) {
            System.out.println("發生錯誤，請檢查圖片路徑與名稱是否正確。錯誤訊息：" + e.getMessage());
        }
    }

    // 輔助方法：將 RGB 色彩轉換為單一的灰階值
    private static int getGrayScale(int rgb) {
        Color c = new Color(rgb);
        return (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
    }

    private static boolean isWebP(File file) {
        try (var input = java.nio.file.Files.newInputStream(file.toPath())) {
            byte[] header = new byte[12];
            if (input.read(header) != header.length) {
                return false;
            }
            return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        } catch (IOException e) {
            return false;
        }
    }
}