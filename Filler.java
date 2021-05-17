class Filler extends Instruction{
    public int rs;
    public int rt;
    public int rd;
    public int shamt;
    public int funct;

    public Filler(int op) {
        super(-1);
        if(op == -1)
            this.instr_name = "empty";
        else if (op == -2)
            this.instr_name = "stall";
        else
            this.instr_name = "squash";
        this.rs = -1;
        this.rt = -1;
        this.rd = -1;
        this.shamt = -1;
        this.funct = -1;
    }

    public void execute(){
        return;
    }
    public void printBinary()
    {
        return;
    }

}