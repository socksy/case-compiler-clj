.class public Main
.super java/lang/Object

.method public <init>()V
	aload_0
	invokenonvirtual java/lang/Object/<init>()V
	return
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 5
	getstatic      java/lang/System/out Ljava/io/PrintStream;
	;ldc            "Hello World."
	bipush 20
	bipush		10
	iadd
	invokevirtual  java/io/PrintStream/println(I)V
	return
.end method
