import java.io.File;
import java.io.FileNotFoundException;
import java.lang.*;
import java.util.*;
import java.io.FileInputStream;

class lab4
{
    final int initAddress = 0; // this is the address of the first instruction in MIPS
    static HashMap<String, Integer[]> rcodes = new HashMap<String, Integer[]>();
    static HashMap<String, Integer[]> icodes = new HashMap<String, Integer[]>();
    static HashMap<String, Integer[]> jcodes = new HashMap<String, Integer[]>();
    static HashMap<String, Integer> labels = new HashMap<String, Integer>();
    static HashMap<String, Integer> registers = new HashMap<String, Integer>();
    static HashMap<Integer, String> reversed = new HashMap<Integer, String>();
    static ArrayList<Instruction> instructions = new ArrayList<Instruction>();
    static ArrayList<Instruction> pipes = new ArrayList<Instruction>();
    static int[] registerList = new int[33];
    static int[] memory = new int[8192];

    static int cycles = 0;
    static int instCount = 0;
    static int delay = 0;

    static void putValues(char prefix, int start, int end, int offset, HashMap<String, Integer> map){
        for(int i = start; i <= end; i++)
            map.put(prefix + String.valueOf(i), i + offset);
    }
    private static void initMap()
    {
        // value = array {opcode, funct, FORMATCODE} (decimal)
        //FORMATCODE
        //1 = R
        //2 = I
        //3 = J
        rcodes.put("and", new Integer[] {0,36,1});
        rcodes.put("or", new Integer[] {0,37,1});
        rcodes.put("add", new Integer[] {0,32,1});
        rcodes.put("sll", new Integer[] {0,0,1});
        rcodes.put("sub", new Integer[] {0,34,1});
        rcodes.put("slt", new Integer[] {0,42,1});
        rcodes.put("jr", new Integer[] {0,8,1});

        icodes.put("addi", new Integer[] {8,0,2});
        icodes.put("beq", new Integer[] {4,0,2});
        icodes.put("bne", new Integer[] {5,0,2});
        icodes.put("lw", new Integer[] {35,0,2});
        icodes.put("sw", new Integer[] {43,0,2});

        jcodes.put("j", new Integer[] {2,0,3});
        jcodes.put("jal", new Integer[] {3,0,3});

        registers.put("zero", 0);
        registers.put("0", 0);
        registers.put("v0", 2);
        registers.put("v1", 3);
        registers.put("a0", 4);
        registers.put("a1", 5);
        registers.put("a2", 6);
        registers.put("a3", 7);
        registers.put("t0", 8);
        registers.put("t1", 9);
        putValues('t', 0, 7, 8, registers);
        putValues('s', 0, 7, 16, registers);
        registers.put("t8", 24);
        registers.put("t9", 25);
        registers.put("sp", 29);
        registers.put("ra", 31);

        for(Map.Entry<String, Integer> entry : registers.entrySet()){
            reversed.put(entry.getValue(), "$" + entry.getKey());
        }

    }

    private static int removeWhiteSpace(String line, int len)
    {
        // remove all whitespace
        line = line.trim();
        // get length of line
        len = line.length();
        // check if line is null or empty after trim
        if((!line.trim().isEmpty() && line != null) && line.charAt(0) == '#'){
            //System.out.println("We found a comment!");
            return -1;
        }
        return len;
    }

    private static boolean grabLabels(int count, String line)
    {
        int index;
        String label = null;

        // check to see if there is a label somewhere to be parsed
        if((index = line.indexOf(':')) != -1) {
            // get the substring
            label = line.substring(0, index);
            //System.out.println("Label " + label + " Found! Line " + count + " is: " + line + " Index is: " + index);
            // append to dictionary
            if(label.indexOf('#') == -1)
                labels.put(label, count);
            // we found a label, check to see if the line is blank (ie just a comment)
            // if so, don't increment count
            if(!validLine(line, index + 1)) {
                //System.out.println("Line continues on next one");
                return false;
            }
        }
        return true;
    }

    private static void findLabels(String[] args) throws FileNotFoundException
    {
        // line count
        int count = 0;
        // string to read in
        String line = null;
        String label = null;
        int index = 0;
        int len = 0;
        Scanner scanner = new Scanner(new File(args[0]));
        // read lines in file (first pass)
        while (scanner.hasNextLine()) {
            // creates a String for the file line
            line = scanner.nextLine().trim();

            // remove all whitespace
            if ((len = removeWhiteSpace(line, len)) == -1)
                continue;

            // if the line has been trimmed and is still not empty:
            if(len > 0) {
                // check to see if there is a label somewhere to be parsed
                if((index = line.indexOf(':')) != -1) {
                    // get the substring
                    label = line.substring(0, index);
                    //System.out.println("Label " + label + " Found! Line " + count + " is: " + line + " Index is: " + index);
                    // append to dictionary
                    if(label.indexOf('#') == -1)
                        labels.put(label, count);
                    // we found a label, check to see if the line is blank (ie just a comment)
                    // if so, don't increment count
                    if(!validLine(line, index + 1)) {
                        //System.out.println("Line continues on next one");
                        continue;
                    }
                }
                // only increment count if it is a valid line (non-comment, non-whitespace)
                count++;
            }
            // process the line
        }
        scanner.close();
    }

    private static void processAssembly(String[] args, String line) throws FileNotFoundException
    {
        Scanner scannerOne = new Scanner(new File(args[0]));
        int count = 0;
        int hashIndex;
        String opcode;
        int labelIndex;
        List<String> instParts;
        Instruction inst;
        int ln;
        int dest;
        int r1;
        int r2;
        int immediate;
        int addr;

        while (scannerOne.hasNextLine()) {

            // remove all whitespace
            line = scannerOne.nextLine().trim();
            hashIndex = line.indexOf('#');
            if (hashIndex != -1)
                line = line.substring(0, hashIndex);

            if (line.length() == 0) // removes all remaining whitespace and checks the length
                continue;

            labelIndex = line.indexOf(':');

            if (labelIndex != -1) {
                line = line.substring(labelIndex + 1, line.length());
                line = line.trim();
            }

            if (line.length() == 0) // removes all remaining whitespace and checks the length
                continue;
            instParts = Arrays.asList(line.split("[:, $()]+")); //splits line by whitespace and commas
            opcode = instParts.get(0);
            opcode = opcode.trim();
            // System.out.println("Opcode is: " + opcode + " count is: " + count);
            if(rcodes.containsKey(opcode)){
                //System.out.println("Opcode is R-format");
                ln = instParts.size();
                if(ln == 2){
                    // jr instruction detected
                    dest = 0;
                    r1 = registers.get(instParts.get(1));
                    r2 = 0;
                }
                else {
                    dest = registers.get(instParts.get(1));
                    r1 = registers.get(instParts.get(2));
                    if (registers.containsKey(instParts.get(3)))
                        r2 = registers.get(instParts.get(3));
                    else
                        r2 = Integer.parseInt(instParts.get(3));
                }
                RFormat r = new RFormat((int)rcodes.get(opcode)[0], dest, r1, r2, (int)rcodes.get(opcode)[1]);
                // r.printBinary();
                instructions.add(r);
            }
            else if(icodes.containsKey(opcode)){
                //System.out.println("Opcode is I-format");
                for(int j = 0; j < instParts.size(); j++){
                    //System.out.println("part is : " + instParts.get(j));
                }
                //r1 was here
                if(opcode.equals("sw") || opcode.equals("lw"))
                {
                    Collections.swap(instParts, 2, 3);
                    Collections.swap(instParts, 1, 2);
                }
                r1 = registers.get(instParts.get(1));
                r2 = registers.get(instParts.get(2));
                if(labels.containsKey(instParts.get(3).trim()))
                    immediate = labels.get(instParts.get(3).trim()) - (count + 1);
                else
                    immediate = Integer.parseInt(instParts.get(3).trim());
                IFormat i = new IFormat ((int)icodes.get(opcode)[0], r1, r2, immediate);
                //System.out.println("imm is " + immediate);
                // i.printBinary();
                instructions.add(i);
            }
            else if(jcodes.containsKey(opcode)){
                //System.out.println("Opcode is J-format");
                if(labels.containsKey(instParts.get(1).trim()))
                    addr = labels.get(instParts.get(1).trim());
                else
                    addr = Integer.parseInt(instParts.get(1));
                JFormat j = new JFormat((int)jcodes.get(opcode)[0], addr);
                instructions.add(j);
            }
            else{
                System.out.println("invalid instruction: " + opcode);
                return;
            }

            count ++; //
        }
        scannerOne.close();
    }

    private static boolean validLine(String line, int offset){
        // goes through the line from offset + 1 to end.
        // if no alphanumerica chars are found to the left of a "#", then
        // we have a "blank" line with a label, so we return false
        int len = line.length();
        char c = 0;
        for (int i = offset + 1; i < len; i++){
            c = line.charAt(i);
            if(c == '#')
                return false;
            // if there is a command on line, return True
            if(Character.isLetterOrDigit(c))
                return true;
        }
        // if we reach the end without finding a '#' or alphanumeric
        // line is empty (whitespace)
        return false;
    }

    private static void printRegisters(){
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        String first;
        String second;
        String third;
        String fourth;
        // prints the contents of the registers...
        System.out.print("\npc = " + registerList[32]);
        for(int i = 0; i < 32; i++){
            if(indexes.size() == 4){
                first = reversed.get(indexes.get(0));
                second = reversed.get(indexes.get(1));
                third = reversed.get(indexes.get(2));
                fourth = reversed.get(indexes.get(3));
                System.out.print("\n" + first + " = " + registerList[indexes.get(0)]);
                System.out.print("        " + second + " = " + registerList[indexes.get(1)]);
                System.out.print("        " + third + " = " + registerList[indexes.get(2)]);
                System.out.print("        " + fourth + " = " + registerList[indexes.get(3)]);
                indexes.clear();
            }
            if(reversed.containsKey(i))
                indexes.add(i);
        }
        first = reversed.get(indexes.get(0));
        second = reversed.get(indexes.get(1));
        third = reversed.get(indexes.get(2));
        System.out.print("\n" + first + " = " + registerList[indexes.get(0)]);
        System.out.print("        " + second + " = " + registerList[indexes.get(1)]);
        System.out.print("        " + third + " = " + registerList[indexes.get(2)] + "\n\n");
    }

    private static void clearAll(){
        Arrays.fill(registerList, 0);
        Arrays.fill(memory, 0);
        System.out.println("        Simulator reset");
    }

    private static void printMemory(String input){
        List<String> instParts = Arrays.asList(input.split(" "));
        int start = Integer.parseInt(instParts.get(1));
        int end = Integer.parseInt(instParts.get(2));
        for(int i = start; i <= end; i++){
            System.out.println("[" + i + "] = " + memory[i]);
        }
    }

    private static void singleStep()
    {

        // if instruction is conditional branch
            // if the delay variable is == -1
                // set the delay variable to 3
            // if the delay variable is > 0
                // enqueue the next chronological inst into the pipe queue
                // print the status of the updated pipe queue
                // decrement the delay variable
            // else if the delay variable is == 0 && branch is taken
                // set appropriate pipes to "squash"
                // print the status of the updated pipe queue
                // set delay variable to -1
                // execute the beq inst.
            // else if the delay variable is == 0 && branch is not taken
                // print the status of the updated pipe queue
                // set delay variable to -1
                // execute the beq and next 3 steps

        // if instruction is jump
            // if the delay variable is == -1
                //set the delay variable to 1
            // if the delay variable is > 0
                // place the "squash" into the first pipe
                // print the updated status of the pipe queue
                // decrement the delay variable
            // else if the delay variable is == 0
                // print the updated status of the pipe queue
                // set delay variable to -1
                // execute the jump inst.

        // if instruction is lw
            // if the delay variable is == -1
                // set the delay variable to 2
            // if the delay variable is > 0
                // enqueue the next chronological inst. into the pipe queue
                // print the status of the pipe queue
                // decrement the delay variable


        instructions.get(registerList[32]).execute();


    }

    private static void multStep(int numLoop)
    {
        for (int i = 0; i < numLoop; i++)
        {
            singleStep();
        }

    }

    private static void step(String input)
    {
        int numLoop = 1;
        List<String> instParts = Arrays.asList(input.split(" "));
        if (instParts.size() > 1) {
            numLoop = Integer.parseInt(instParts.get(1));
            multStep(numLoop);
        }
        else
            singleStep();

        System.out.println("        " + numLoop + " instruction(s) executed");
    }

    private static void run()
    {
        while (registerList[32] < instructions.size())
        {
            singleStep();
        }
    }

    private static void printHelp()
    {
        System.out.println("\nh = show help");
        System.out.println("d = dump register state");
        System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
        System.out.println("s num = step through num instructions of the program");
        System.out.println("r = run until the program ends");
        System.out.println("m num1 num2 = display data memory from location num1 to num2");
        System.out.println("c = clear all registers, memory, and the program counter to 0");
        System.out.println("q = exit the program\n");
    }

    private static void runSimulator(String[] args)
    {
        if (args.length == 2) {
            try{
                File myFile = new File(args[1]);
                System.setIn(new FileInputStream(myFile));
            }catch(Exception e) {System.out.println(e);}
        }

        Scanner scanner = new Scanner(System.in);
        String input;

        while (true)
        {
            System.out.print("mips> ");
            input = scanner.nextLine();
            if (args.length == 2)
                System.out.print(input + "\n");
            char chr = input.charAt(0);
            if (chr == 'q')
                break;
            else if (chr == 'd')
                printRegisters();
            else if (chr == 'm')
                printMemory(input);
            else if (chr == 's') {
                step(input);
            }
            else if (chr == 'r')
                run();
            else if (chr == 'h')
                printHelp();
            else if (chr == 'c')
                clearAll();
        }
    }

    private static void printPipes(){
        System.out.print("\npc\t\tif/id\t\tid/exe\t\texe/mem\t\tmem/wb\n");
        System.out.print(registerList[32] + "\t\t");
        System.out.print(pipes[0].instr_name + "\t\t"+ pipes[1].instr_name);
        System.out.print("\t\t" + pipes[2].instr_name + "\t\t" + pipes[3].instr_name);
    }

    public static void main(String[] args)
    {
        initMap();
        // line count
        int count = 0;
        // string to read in
        String line = null;
        String label = null;
        int index = 0;

        if(args.length == 0 || args.length > 2){
            System.out.println("Usage: lab3 inputFile <script>");
            return;
        }

        try {
            // creates a hashMap of labels (first pass)
            /* --------------------------------------------------------------------------- */

            findLabels(args);

            // read lines in file (second pass)
            /* --------------------------------------------------------------------------- */

            processAssembly(args, line);

        }
        catch(FileNotFoundException e){
            System.out.println("File not found!");
            return;
        }
        System.out.println("");
        runSimulator(args);
    }
}
