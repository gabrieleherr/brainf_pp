import java.io.*;
import java.util.*;
public class Interpreter{
    private static int limit = 1024;
    private static short[] memory;
    private static HashMap<String, String> funcMap;
    private static BufferedReader inputReader;
    public static void main(String[] args) throws Exception{
        BufferedReader buffer = new BufferedReader(new FileReader("test_file.bfpp"));
        funcMap = new HashMap<>();
        char read;
        String funcname = "";
        StringBuilder func = new StringBuilder();
        boolean infunc = false;
        while(buffer.ready()){
            read = (char) buffer.read();
            if (Character.isWhitespace(read)){
                continue;
            }
            if (read == '{' && !infunc){
                infunc = true;
                func = new StringBuilder();
                do{
                    read = (char)buffer.read();
                }while (Character.isWhitespace(read));
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return;
                }
                funcname = read + "" + (char)buffer.read();
                if (!Character.isLetter(funcname.charAt(1))){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return;
                }
                continue;
            }
            if (read == '}' && infunc){
                if (funcMap.containsKey(funcname)){
                    System.out.println("ERROR: function " + funcname + " defined twice");
                    buffer.close();
                    return;
                }
                funcMap.put(funcname, func.toString());
                infunc = false;
                continue;
            }
            if (!infunc){
                continue;
            }
            if (read == '('){
                do{
                    read = (char)buffer.read();
                } while (Character.isWhitespace(read));
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return;
                }
                func.append(read);
                read = (char)buffer.read();
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return;
                }
                func.append(read);
                continue;
            }
            if (read == '{'){
                System.out.println("ERROR: \'{\' found inside function declaration");
                buffer.close();
                return;
            }
            if (read == '+' || read == '-' || read == '[' || read == ']' || read == '<' || read == '>' 
                || read == ',' || read == '.' || read == ';'){
                func.append(read);
            }
        }
        buffer.close();
        inputReader = new BufferedReader(new FileReader("test_input.txt"));
        System.out.println(funcMap);
        memory = new short[limit];
        run("mn", 0);
        System.out.println();
    }
    private static short run(String name, int startindex) throws Exception {
        int cellPointer = startindex;
        int maxPointer = startindex;
        int codePointer = 0;
        String operations = funcMap.get(name);
        // System.out.println(name + " " + operations);
        Stack<Integer> jumpIndexes = new Stack<>();
        while (codePointer < operations.length()){
            switch(operations.charAt(codePointer)){
                case ';': return memory[cellPointer];
                case '+': memory[cellPointer] ++; break;
                case '-': memory[cellPointer] --; break;
                case '<': cellPointer --; break;
                case '>': cellPointer ++; maxPointer = Math.max(maxPointer, cellPointer); break;
                case '.': System.out.print((char) memory[cellPointer]); break;
                case ',': memory[cellPointer] = (short) inputReader.read(); break;
                case '[': jumpIndexes.add(codePointer); break;
                case ']': 
                if (memory[cellPointer] != 0) codePointer = jumpIndexes.peek() - 1;
                else jumpIndexes.pop();
                break;
                // function name
                default:
                if (maxPointer == limit - 1){
                    System.out.println("ERROR: cell index out of bounds on function call");
                    return -1;
                }
                memory[maxPointer + 1] = memory[cellPointer];
                memory[cellPointer] = run(operations.substring(codePointer, codePointer + 2), maxPointer + 1);
                codePointer ++;
                if (memory[cellPointer] == -1) return -1;
                for (int x = maxPointer + 1; x < limit; x ++) memory[x] = 0;
            }
            codePointer ++;
            if (startindex > cellPointer || cellPointer >= limit){
                System.out.println("ERROR: cell index out of bounds");
                return -1;
            }
            if (memory[cellPointer] < 0){
                memory[cellPointer] = (short) (Short.MAX_VALUE + memory[cellPointer] + 1);
            }
        }
        return memory[cellPointer];
    }
}