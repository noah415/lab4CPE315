class JFormat extends Instruction
{
    private int address;

    public JFormat(int opcode, int address){
        super(opcode);
        this.address = address;
        if(opcode == 2)
            this.instr_name = "j";
        if(opcode == 3)
            this.instr_name = "jal";
    }

    // TODO: insert logic for calculating the address of label

    // TODO: insert logic for printing in binary
    public void printBinary()
    {
      String op = Instruction.bTS(opcode, 6);
      String sadd = Instruction.bTS(address, 26);
      System.out.println(op + " " + sadd);
    }

    public void execute()
    {
        switch(opcode)
        {
            case 2:
                j();
                break;
            case 3:
                jal();
                break;
        }
    }

    private void j() { lab4.registerList[32] = address; }

    private void jal() {
        lab4.registerList[31] = lab4.registerList[32] + 1;
        lab4.registerList[32] = address;
    }
}
