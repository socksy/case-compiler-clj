.class public Int
.super java/lang/Object

.method public <init>()V
	aload_0
	invokenonvirtual java/lang/Object/<init>()V
	return
.end method

.method public run()LType;
	.limit stack 10
	.limit locals 10
	new Type
	dup
	invokespecial Type/<init>()V
	astore_2 ;Type is in var 2
	aload_2
	iconst_0 ;int type
	putfield Type/ident I
	aload_2
	iconst_0 ;int constructor (no constructor, really)
	putfield Type/constructorType I
	aload_2
	iconst_1
	anewarray java/lang/Object
	dup
	iconst_0
	new java/lang/Integer
	dup
	iconst_5
	invokespecial java/lang/Integer/<init>(I)V
	aastore
	putfield Type/values [Ljava/lang/Object;
	aload_2
	areturn
.end method
