class Filler extends Instruction{

    public Filler(int op) {
        super(-1);
        if(op == -1)
            this.instr_name = "empty";
        else if (op == -2)
            this.instr_name = "stall";
        else
            this.instr_name = "squash";
    }

    public void execute(){
        return
    }
    public void printBinary()
    {
        return
    }

}