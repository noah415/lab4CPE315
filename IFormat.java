class IFormat extends Instruction{
    public int rs;
    public int rt;
    public int immediate;

    public IFormat(int opcode, int rs, int rt, int immediate){
        super(opcode);
        this.rs = rs;
        this.rt = rt;
        this.immediate = immediate;
        switch(opcode)
        {
            case 8:
                this.instr_name = "addi";
                break;
            case 4:
                this.instr_name = "beq";
                break;
            case 5:
                this.instr_name = "bne";
                break;
            case 35:
                this.instr_name = "lw";
                break;
            case 43:
                this.instr_name = "sw";
                break;
        }
    }

    // TODO: insert logic for calculating the address of label (if needed)

    // TODO: insert logic for printing in binary
    public void printBinary()
    {
      String op = Instruction.bTS(opcode, 6);
      String srs = Instruction.bTS(rs, 5);
      String srt = Instruction.bTS(rt, 5);
      String simm = Instruction.bTS(immediate, 16);
      System.out.println(op + " " + srs + " " + srt + " " + simm);
    }

    public void execute()
    {
        switch(opcode)
        {
            case 8:
                addi();
                lab4.registerList[32]++;
                break;
            case 4:
                lab4.registerList[32]++;
                beq();
                break;
            case 5:
                lab4.registerList[32]++;
                bne();
                break;
            case 35:
                lw();
                lab4.registerList[32]++;
                break;
            case 43:
                sw();
                lab4.registerList[32]++;
                break;
        }
    }

    // may be wrong registers

    private void addi(){ lab4.registerList[rs] = lab4.registerList[rt] + immediate; }

    private void beq(){
        if (lab4.registerList[rs] == lab4.registerList[rt]) {
            lab4.registerList[32] = lab4.registerList[32] + immediate;
            lab4.taken = true;
            lab4.taken_count = 2;
        }
    }

    private void bne(){
        if (lab4.registerList[rs] != lab4.registerList[rt]) {
            lab4.registerList[32] = lab4.registerList[32] + immediate;
            lab4.taken = true;
            lab4.taken_count = 2;
        }
    }

    private void lw(){
        //System.out.println("rt: " + rt + ", rs: " + rs + ", imm: " + immediate);
        lab4.registerList[rt] = lab4.memory[lab4.registerList[rs] + immediate];
    }

    private void sw(){ lab4.memory[lab4.registerList[rs] + immediate] = lab4.registerList[rt]; }
}
