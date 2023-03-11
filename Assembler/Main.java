import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final String[] instructionSet = {
        "ADD", "SUB", "AND", "OR", "XOR", "LD", "ST", "JUMP", 
        "ADDI", "SUBI", "ANDI", "ORI", "XORI", "PUSH", "POP", 
        "BE", "BNE" 
    };
    private final int immBit = 7;
    private final int addressBit = 10;

    private final static String inputFile = "input.txt";
    private final String outputFile = "output.txt";

    public static void main(String[] args) {
        Main main = new Main();
        main.fileOperations(inputFile);
    }

    private void fileOperations(String fileName) {
        try {
            File file = new File(fileName);
            Scanner input = new Scanner(file);
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.append("v2.0 raw\n");
            
            while (input.hasNextLine()) {
                String data = input.nextLine();
                fileWriter.append(converter(data) + "\n");
            }
            
            input.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String converter(String data) {
        String[] parseData = data.replaceAll(",", "").split(" ");
        String opcode = parseData[0];

        StringBuilder sb = new StringBuilder();
        sb.append(opcodeToBinary(opcode));

        switch (opcode) {
            case "ADD", "SUB", "AND", "OR", "XOR" -> {
                sb.append(registerToBinary(parseData[1])); // DST
                sb.append(registerToBinary(parseData[2])); // SRC1
                sb.append("000");
                sb.append(registerToBinary(parseData[3])); // SRC2
            }
            case "ADDI", "SUBI", "ANDI", "ORI", "XORI" -> {
                sb.append(registerToBinary(parseData[1])); // DST
                sb.append(registerToBinary(parseData[2])); // SRC1
                sb.append(immOrAddrToBinary(parseData[3], true)); // IMM
            }
            case "LD", "ST" -> {
                sb.append(registerToBinary(parseData[1])); // DST/SRC1
                sb.append("0");
                sb.append(immOrAddrToBinary(parseData[2], false)); // ADDR
            }
            case "JUMP" -> {
                sb.append("00000");
                sb.append(immOrAddrToBinary(parseData[1], false)); // ADDR 
            }
            case "PUSH", "POP" -> {
                sb.append(registerToBinary(parseData[1])); // SRC1
                sb.append("00000000000");
            }
            case "BE", "BNE" -> {
                sb.append(registerToBinary(parseData[1])); // DST
                sb.append(registerToBinary(parseData[2])); // SRC1
                sb.append(immOrAddrToBinary(parseData[3], false)); // ADDR
            }
        }
        return convertToHex(sb.toString()) ;
    }

    private String opcodeToBinary(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < instructionSet.length; i++) {
            if (instructionSet[i].equals(value)) {
                if (i < 8) { // "ADD", "SUB", "AND", "OR", "XOR", "LD", "ST", "JUMP"
                    result.append("00");
                    result.append(intTo3BitBinary(i));
                }
                else if (i < 15) { // "ADDI", "SUBI", "ANDI", "ORI", "XORI", "PUSH", "POP"
                    result.append("01");
                    result.append(intTo3BitBinary(i-8)); // (i-8) it resets for opcode '01'
                }
                else if (i == 15) { // "BE"
                    result.append("10");
                }
                else if (i == 16) { // "BNE"
                    result.append("11");
                }
                return result.toString();
            }
        }
        result.append("Opcode not found!");
        return result.toString();
    }

    private String intTo3BitBinary(int i) {
        if (Integer.toBinaryString(i).length() == 1)
            return "00" + Integer.toBinaryString(i);
        else if (Integer.toBinaryString(i).length() == 2)
            return "0" + Integer.toBinaryString(i);
        else if (Integer.toBinaryString(i).length() == 3)
            return Integer.toBinaryString(i) ;
        else
            return "Integer to 3 bit binary conversion error!";
    }

    private String registerToBinary(String value) {
        // We have 16 registers so we need 4 bits for representing the registers 
        // R0->0000 R1->0001 .. R15->1111
        String[] values = value.split("R");
        int registerValue = Integer.parseInt(values[1]) ;

        if (Integer.toBinaryString(registerValue).length() == 1)
            return "000" + Integer.toBinaryString(registerValue);
        else if (Integer.toBinaryString(registerValue).length() == 2)
            return "00" + Integer.toBinaryString(registerValue);
        else if (Integer.toBinaryString(registerValue).length() == 3)
            return "0" + Integer.toBinaryString(registerValue);
        else if (Integer.toBinaryString(registerValue).length() == 4)
            return Integer.toBinaryString(registerValue);
        else
            return "Register not found!";
    }

    private String immOrAddrToBinary(String value, Boolean isImm) {
        StringBuilder sb = new StringBuilder();
        int bitSize = isImm == true ? immBit : addressBit;

        if (value.startsWith("-")) { // Negative
            StringBuilder tempSb = new StringBuilder();
            String[] splitSign = value.split("-");
            int number = Integer.parseInt(splitSign[1]) - 1;
            
            for (int i = 0; i < bitSize - Integer.toBinaryString(number).length(); i++)
                tempSb.append("0");
            
            tempSb.append(Integer.toBinaryString(number));
            
            for (int i = 0; i < tempSb.toString().length(); i++) {
                if (tempSb.toString().charAt(i) == '0')  
                    sb.append("1");
                else if(tempSb.toString().charAt(i) == '1')
                    sb.append("0");
            }
        } 
        else { // Positive
            for (int i = 0; i < bitSize - Integer.toBinaryString(Integer.parseInt(value)).length(); i++)
                sb.append("0");
            
            sb.append(Integer.toBinaryString(Integer.parseInt(value)));
        }
        return sb.toString();
    }

    private String convertToHex(String binaryValue) {
        return Integer.toHexString(convertToDecimal(binaryValue.substring(0,4))).toUpperCase() +
               Integer.toHexString(convertToDecimal(binaryValue.substring(4,8))).toUpperCase() +
               Integer.toHexString(convertToDecimal(binaryValue.substring(8,12))).toUpperCase() +
               Integer.toHexString(convertToDecimal(binaryValue.substring(12,16))).toUpperCase() +
               Integer.toHexString(convertToDecimal(binaryValue.substring(16,20))).toUpperCase();
    }

    private int convertToDecimal(String binaryFour) {
        switch (binaryFour){
            case "0000" -> { return 0; }
            case "0001" -> { return 1; }
            case "0010" -> { return 2; }
            case "0011" -> { return 3; }
            case "0100" -> { return 4; }
            case "0101" -> { return 5; }
            case "0110" -> { return 6; }
            case "0111" -> { return 7; }
            case "1000" -> { return 8; }
            case "1001" -> { return 9; }
            case "1010" -> { return 10; }
            case "1011" -> { return 11; }
            case "1100" -> { return 12; }
            case "1101" -> { return 13; }
            case "1110" -> { return 14; }
            case "1111" -> { return 15; }
        } 
        return 0;
    }
}
