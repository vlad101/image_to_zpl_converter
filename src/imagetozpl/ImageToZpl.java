package imagetozpl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ImageToZpl {
    private int blackLimit = 380;
    private int total;
    private int widthBytes;
    private boolean compressHex = false;
    private static Map<Integer, String> aplMapCode = new HashMap<Integer, String>();
    {
        aplMapCode.put(1, "G");
        aplMapCode.put(2, "H");
        aplMapCode.put(3, "I");
        aplMapCode.put(4, "J");
        aplMapCode.put(5, "K");
        aplMapCode.put(6, "L");
        aplMapCode.put(7, "M");
        aplMapCode.put(8, "N");
        aplMapCode.put(9, "O");
        aplMapCode.put(10, "P");
        aplMapCode.put(11, "Q");
        aplMapCode.put(12, "R");
        aplMapCode.put(13, "S");
        aplMapCode.put(14, "T");
        aplMapCode.put(15, "U");
        aplMapCode.put(16, "V");
        aplMapCode.put(17, "W");
        aplMapCode.put(18, "X");
        aplMapCode.put(19, "Y");
        aplMapCode.put(20, "g");
        aplMapCode.put(40, "h");
        aplMapCode.put(60, "i");
        aplMapCode.put(80, "j");
        aplMapCode.put(100, "k");
        aplMapCode.put(120, "l");
        aplMapCode.put(140, "m");
        aplMapCode.put(160, "n");
        aplMapCode.put(180, "o");
        aplMapCode.put(200, "p");
        aplMapCode.put(220, "q");
        aplMapCode.put(240, "r");
        aplMapCode.put(260, "s");
        aplMapCode.put(280, "t");
        aplMapCode.put(300, "u");
        aplMapCode.put(320, "v");
        aplMapCode.put(340, "w");
        aplMapCode.put(360, "x");
        aplMapCode.put(380, "y");        
        aplMapCode.put(400, "z");            
    }
    public String convertfromImg(BufferedImage image) throws IOException {
        String cuerpo = createBody(image);
        if(compressHex)
           cuerpo = encodeHexAscii(cuerpo);
        return headDoc() + cuerpo + footDoc();        
    }
    private String createBody(BufferedImage orginalImage) throws IOException {
        StringBuffer sb = new StringBuffer();
        Graphics2D graphics = orginalImage.createGraphics();
        graphics.drawImage(orginalImage, 0, 0, null);
        int height = orginalImage.getHeight();
        int width = orginalImage.getWidth();
        int rgb, red, green, blue, index=0;        
        char auxBinaryChar[] =  {'0', '0', '0', '0', '0', '0', '0', '0'};
        widthBytes = width/8;
        if(width%8>0){
            widthBytes= (((int)(width/8))+1);
        } else {
            widthBytes= width/8;
        }
        this.total = widthBytes*height;
        for (int h = 0; h<height; h++)
        {
            for (int w = 0; w<width; w++)
            {
                rgb = orginalImage.getRGB(w, h);
                red = (rgb >> 16 ) & 0x000000FF;
                green = (rgb >> 8 ) & 0x000000FF;
                blue = (rgb) & 0x000000FF;
                char auxChar = '1';
                int totalColor = red + green + blue;
                if(totalColor>blackLimit){
                    auxChar = '0';
                }
                auxBinaryChar[index] = auxChar;
                index++;
                if(index==8 || w==(width-1)){
                    sb.append(fourByteBinary(new String(auxBinaryChar)));
                    auxBinaryChar =  new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
                    index=0;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    private String fourByteBinary(String binaryStr){
        int decimal = Integer.parseInt(binaryStr,2);
        if (decimal>15){
            return Integer.toString(decimal,16).toUpperCase();
        } else {
            return "0" + Integer.toString(decimal,16).toUpperCase();
        }
    }
    private String encodeHexAscii(String code){
        int maxlinea =  widthBytes * 2;        
        StringBuffer sbCode = new StringBuffer();
        StringBuffer sbLinea = new StringBuffer();
        String previousLine = null;
        int counter = 1;
        char aux = code.charAt(0);
        boolean firstChar = false; 
        for(int i = 1; i< code.length(); i++ ){
            if(firstChar){
                aux = code.charAt(i);
                firstChar = false;
                continue;
            }
            if(code.charAt(i)=='\n'){
                if(counter>=maxlinea && aux=='0'){
                    sbLinea.append(",");
                } else     if(counter>=maxlinea && aux=='F'){
                    sbLinea.append("!");
                } else if (counter>20){
                    int multi20 = (counter/20)*20;
                    int resto20 = (counter%20);
                    sbLinea.append(aplMapCode.get(multi20));
                    if(resto20!=0){
                        sbLinea.append(aplMapCode.get(resto20) + aux);    
                    } else {
                        sbLinea.append(aux);    
                    }
                } else {
                    sbLinea.append(aplMapCode.get(counter) + aux);
                    if(aplMapCode.get(counter)==null){
                    }
                }
                counter = 1;
                firstChar = true;
                if(sbLinea.toString().equals(previousLine)){
                    sbCode.append(":");
                } else {
                    sbCode.append(sbLinea.toString());
                }                
                previousLine = sbLinea.toString();
                sbLinea.setLength(0);
                continue;
            }
            if(aux == code.charAt(i)){
                counter++;                
            } else {
                if(counter>20){
                    int multi20 = (counter/20)*20;
                    int resto20 = (counter%20);
                    sbLinea.append(aplMapCode.get(multi20));
                    if(resto20!=0){
                        sbLinea.append(aplMapCode.get(resto20) + aux);    
                    } else {
                        sbLinea.append(aux);    
                    }
                } else {
                    sbLinea.append(aplMapCode.get(counter) + aux);
                }
                counter = 1;
                aux = code.charAt(i);
            }            
        }
        return sbCode.toString();
    }
    private String headDoc(){
        String str = "^XA " +
                        "^FO165,75^GFA,"+ total + ","+ total + "," + widthBytes +", ";
        return str;
    }
    private String footDoc(){
        String str = "^FS"+
                        "^XZ";        
        return str;
    }
    public void setCompressHex(boolean compressHex) {
        this.compressHex = compressHex;
    }
    public void setBlacknessLimitPercentage(int percentage){
        blackLimit = (percentage * 768 / 100);
    }
    public void print(String address, int port, String command) throws IOException {
        char ESC = (char)27; //Ascii character for ESCAPE
        char CR = (char) 13; //Ascii character for Carriage Return
        /////////////
        Socket socket = null;
                OutputStream output= null;
                BufferedReader reader= null;
        ///////////
        try
            { 
                socket = new Socket(address,port);
                socket.setSoTimeout(5000);
                output = socket.getOutputStream();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (UnknownHostException e) {
            }
        /////////
        if (socket != null  && output != null)
            {
                try
                {
                    String cmd=ESC+command+CR;
                    output.write(cmd.getBytes());
                    output.flush();
                    socket.shutdownOutput();

                    output.close();
                    socket.close();
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());

                }
            }
    }
    public static void main(String[] args) throws Exception {
        // 1019845_AM_BMP1.bmp
        // 1019845_AM_JPG80.jpg
        // 1019845_AM_PNG8.png
        BufferedImage orginalImage = ImageIO.read(new File("Z:\\Lugano\\1019845_AM_BMP4.bmp"));
        ImageToZpl zp = new ImageToZpl();
        zp.setCompressHex(true);
        zp.setBlacknessLimitPercentage(50);        
        System.out.println(zp.convertfromImg(orginalImage));       
        zp.print("192.168.18.246", 9100, zp.convertfromImg(orginalImage));
    }
}