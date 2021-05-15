import java.util.*;

class RFormat extends Instruction
{
  public int opcode;
  public int rs;
  public int rt;
  public int rd;
  public int shamt;
  public int funct;

  public RFormat(int opcode, int dest, int op1, int op2, int funct)
  {
    super(opcode);
    this.funct = funct;

    switch(funct)
    {
      case 36:
        this.instr_name = "and";
        break;
      case 37:
        this.instr_name = "or";
        break;
      case 32:
        this.instr_name = "add";
        break;
      case 0:
        this.instr_name = "sll";
        break;
      case 34:
        this.instr_name = "sub";
        break;
      case 42:
        this.instr_name = "slt";
        break;
      case 8:
        this.instr_name = "jr";
        break;
    }

    if (opcode == 0 && funct == 0)
    {
      this.rd = dest;
      this.rs = 0;
      this.rt = op1;
      this.shamt = op2;
    }
    else
    {
      this.rd = dest;
      this.rs = op1;
      this.rt = op2;
      this.shamt = 0;
    }
  }

  public void printBinary()
  {
    String op = Instruction.bTS(opcode, 6);
    String srs = Instruction.bTS(rs, 5);
    String srt = Instruction.bTS(rt, 5);
    String srd = Instruction.bTS(rd, 5);
    String sshamt = Instruction.bTS(shamt, 5);
    String sfunct = Instruction.bTS(funct, 6);
    System.out.println(op + " " + srs + " " + srt + " " + srd + " " + sshamt + " " + sfunct);
  }

  public void execute()
  {
    switch(funct)
    {
      case 36:
        and();
        lab4.registerList[32]++;
        break;
      case 37:
        or();
        lab4.registerList[32]++;
        break;
      case 32:
        add();
        lab4.registerList[32]++;
        break;
      case 0:
        sll();
        lab4.registerList[32]++;
        break;
      case 34:
        sub();
        lab4.registerList[32]++;
        break;
      case 42:
        slt();
        lab4.registerList[32]++;
        break;
      case 8:
        jr();
        break;
    }
  }

  private void add()
  {
    lab4.registerList[rd] = lab4.registerList[rs] + lab4.registerList[rt];
  }

  private void and()
  {
    lab4.registerList[rd] = lab4.registerList[rs] & lab4.registerList[rt];
  }

  private void jr()
  {
    lab4.registerList[32] = lab4.registerList[rs];
  }

  private void or()
  {
    lab4.registerList[rd] = lab4.registerList[rs] | lab4.registerList[rt];
  }

  private void slt()
  {
    lab4.registerList[rd] = (lab4.registerList[rs] < lab4.registerList[rt]) ? 1 : 0;
  }

  private void sll()
  {
    lab4.registerList[rd] = lab4.registerList[rt] << shamt;
  }

  private void sub()
  {
    lab4.registerList[rd] = lab4.registerList[rs] - lab4.registerList[rt];
  }
}
