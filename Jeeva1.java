class Jeeva{
    int a,b,c;
    void sum(){
        c=a+b;
        System.out.println(c);
    }
}
class Jeeva1{
    public static void main(String[] args){
        Jeeva obj=new Jeeva();
        Jeeva obj1=new Jeeva();
        obj.a=10;
        obj.b=5;
        obj.sum();
        obj1.a=20;
        obj1.b=10;
        obj1.sum();
    }
}