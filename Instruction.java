abstract class Instruction
{
  public int opcode;

  public Instruction(int opcode)
  {
    this.opcode = opcode;
  }

  protected void printSect(int arr[])
  {
    for (int bit : arr)
    {
      System.out.print(bit);
    }
    System.out.print("\n");
  }

  abstract public void printBinary();
  abstract public void execute();

  public static String bTS(int num, int length){
    String str = "";
    int counter = 0;
    boolean isPos = (num >= 0);

    while(num != 0 && counter < length){
      if((num % 2) == 0)
        str = "0" + str;
      else
        str = "1" + str;
      num = num >>> 1;
      counter++;
    }

    while(counter < length){
      // if isPos is true, append a zero
      if(isPos)
        str = "0" + str;
      // else append a one
      else
        str = "1" + str;
      counter++;
    }
    return str;
  }
}