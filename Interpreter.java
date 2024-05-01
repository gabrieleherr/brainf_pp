import java.io.*;
import java.util.*;
public class Interpreter{
    private int limit = 26;
    private int global = 1;
    private boolean debug = false;
    private short[] memory;
    private HashMap<String, String> funcMap;
    private BufferedReader inputReader;
    private StringBuilder output;
    //public static void main(String[] args) throws Exception{
    public String initcode(int limit, int global, String source) throws Exception{
        this.limit = limit; this.global = global;
        output = new StringBuilder();
        BufferedReader buffer = new BufferedReader(new FileReader("test_file.bfpp"));
        funcMap = new HashMap<>();
        char read;
        String funcname = "";
        StringBuilder func = new StringBuilder();
        boolean infunc = false;
        while(buffer.ready()){
            read = (char) buffer.read();
            if (Character.isWhitespace(read)) continue;
            if (read == '{' && !infunc){
                infunc = true;
                func = new StringBuilder();
                do{
                    read = (char)buffer.read();
                }while (Character.isWhitespace(read));
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return "";
                }
                funcname = read + "" + (char)buffer.read();
                if (!Character.isLetter(funcname.charAt(1))){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return "";
                }
                continue;
            }
            if (read == '}' && infunc){
                if (funcMap.containsKey(funcname)){
                    System.out.println("ERROR: function " + funcname + " defined twice");
                    buffer.close();
                    return "";
                }
                funcMap.put(funcname, func.toString());
                infunc = false;
                continue;
            }
            if (!infunc) continue;
            if (read == '('){
                do{
                    read = (char)buffer.read();
                } while (Character.isWhitespace(read));
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return "";
                }
                func.append(read);
                read = (char)buffer.read();
                if (!Character.isLetter(read)){
                    System.out.println("ERROR: function name must be two letters");
                    buffer.close();
                    return "";
                }
                func.append(read);
                continue;
            }
            if (read == '{'){
                System.out.println("ERROR: \'{\' found inside function declaration");
                buffer.close();
                return "";
            }
            if (read == '+' || read == '-' || read == '[' || read == ']' || read == '<' || read == '>' 
                || read == ',' || read == '.' || read == ';' || read == '"' && debug){
                func.append(read);
            }
        }
        buffer.close();
        // source = "test_input.txt";
        inputReader = new BufferedReader(new FileReader(source));
        // System.out.println(funcMap);
        memory = new short[limit + global];
        run("mn", global);
        // System.out.println(output);
        inputReader.close();
        return output.toString();
    }
    private short run(String name, int startindex) throws Exception {
        int cellPointer = startindex;
        int maxPointer = startindex;
        int codePointer = 0;
        String operations = funcMap.get(name);
        Stack<Integer> jumpIndexes = new Stack<>();
        while (codePointer < operations.length()){
            switch(operations.charAt(codePointer)){
                case ';':
                return memory[cellPointer];
                case '+': memory[cellPointer] ++; break;
                case '-': memory[cellPointer] --; break;
                case '<': 
                if (cellPointer == startindex) cellPointer = global;
                cellPointer --; break;
                case '>': 
                if (cellPointer == global - 1) cellPointer = startindex - 1;
                cellPointer ++; maxPointer = Math.max(maxPointer, cellPointer); break;
                case '.': //System.out.print((char) memory[cellPointer]); 
                output.append((char) memory[cellPointer]); break;
                case ',': 
                if (!inputReader.ready()) memory[cellPointer] = 0;
                else memory[cellPointer] = (short) inputReader.read(); 
                break;
                case '[': 
                if (memory[cellPointer] == 0){
                    int runningtotal = 1;
                    while (runningtotal > 0 && codePointer < operations.length() - 1){
                        if (operations.charAt(codePointer + 1) == ']') runningtotal --;
                        else if (operations.charAt(codePointer + 1) == '[') runningtotal ++;
                        codePointer ++;
                    }
                    if (codePointer == operations.length() - 1 && operations.charAt(codePointer) != ']'){
                        System.out.println("ERROR: unclosed loop");
                        return -1;
                    }
                }
                else jumpIndexes.add(codePointer); 
                break;
                case ']': 
                if (memory[cellPointer] != 0) codePointer = jumpIndexes.peek();
                else jumpIndexes.pop();
                break;
                case '"':
                System.out.print(memory[cellPointer] + " ");
                break;
                default:
                // System.out.println(Arrays.toString(memory));
                // function name
                while (maxPointer >= 0 && memory[maxPointer] == 0) maxPointer --;
                if (maxPointer < cellPointer) maxPointer = cellPointer;
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
            if (0 > cellPointer || cellPointer >= limit){
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