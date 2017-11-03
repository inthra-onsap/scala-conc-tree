# Conc-Tree

Intermediate balanced tree data structure for parallel processing in Scala.

#### Properties
(<> means internal node)
1) The height differences between Left sub-tree and Right sub-tree must not be greater than 1. (Same as AVL tree)
2) The <> node must never contain Empty object as left or right child.


#### To concatenate 2 trees and maintain their properties, It has following criterions:

##### Concat method: O(log m - log n) 

(T1 = tree no.1, T2 = tree no.2, m = total nodes of T1, n is total nodes of T2)

1) If the height of 2 trees are different within range of 1 (T2.level - T1.level) then create new <> node to concatenate T1 and T2 together.
2) If the height of 2 trees are different greater than 1:
    - **case 1)** T1's left sub-tree has height greater than or equal to T1's right sub-tree then recursive concatenate the T1's right sub-tree with T2 to get T3. Finally concatenate T1.left and T3 together.
    - **case 2)** T1's left sub-tree has height lower than T1's right sub-tree then split T1 into 3 sub-trees as following. 
        1) T1.left
        2) T1.right.left
        3) T1.right.right
        
        Recursive concatenate T1.right.right and T2 together to get T3.
        
        **If T1 and T3(T1.level - T3.level) has height difference between 0 and 1** then concatenate T1.left and T1.right.left together to get T4.
        Finally concatenate T4 and T3.
        
        **Otherwise** concatenate T1.right.left and T3 together to get T4. Finally concatenate T1.left and T4.
        
**Remark:** T1 always has height greater than or equal to T2.

##### AppendLeaf method: O(1) - amortized time
Normally if we didn't implement Append node. To append an element would take O(log n) which is height of the tree. 
The Append node is the special node that does NOT have to follow the property rule 1. It can have height of left and right child difference greater than 1.
Once it is the time to balance the tree to conform the property rules above, It take amortized time complexity O(1).

The implementation concept: We use the counting system of binary number to represent the tree structure. The system is very similar to Binommial Tree for keeping sub-trees.

**Append node** is the lazy evaluated node which it does not adhere to the property rules above.
But they sometimes be deleted and replaced by <>(internal) node and the tree will be back to balacned and conform to the property rules.
Whenever you want to use the Conc-Tree. Don't forget to re-balance the tree to get rid of the Append nodes and having ONLY leafs and <>(internal) nodes in the tree. 

###### Here is the example how it works:



    Step 1) Add leaf (10). The binary representation is "1" for 1 leaf. 
            It means the current binary bit representation is big enough to keep one single leaf. 
            Don't need to re-balance. 
        
        Scala View: val t1 = ConcTree.appendLeaf(Empty, new Single[Int](10))   
                     
        Binary View:              1
        Tree View:              (10)
           
           
    Step 2) Add leaf (20). The binary representation is "10" for 2 leafs. 
            The previous binary bit representation which is "01" is not enough for keeping 2 leafs. 
            So we will concatenate (10) and (20) leafs together by <A>(internal) node 
            and leave the right-most bit as 0 number of leaf.
            
        Scala View: val t2 = ConcTree.appendLeaf(t1, new Single[Int](20))
          
        Binary View:                1                                           1          0
        Tree View:                (10) append (20)         transform to        <A>
                                                                              /   \
                                                                             /     \
                                                                           (10)   (20)   empty
    
    
    Step 3) Add leaf (30). The binary representation is "11" for 3 leafs. The previous binary bit representation 
            which is "10" is enough for keeping 3 leafs just append (30) without carrying to left most bit.
            So we will concatenate it by Append node. 
        
        Scala View: val t3 = appendLeaf(t2, new Single[Int](30))
        
        Binary View:               1               0                             1        1
        Tree View:                <A>
                                 /   \                                            Append[T]
                                /     \                                            /    \
                              (10)   (20) append (30)      transform to           /      \
                                                                                <A>     (30)
                                                                               /   \
                                                                              /     \
                                                                            (10)   (20)
                            
      
    Step 4) Add leaf (40). The binary representation is "100" for 4 leafs. 
            The previous binary bit representation which is "11" is not enough for keeping 4 leafs. 
            We need to re-balance the existing tree to conform the property rules above 
            by deleting the Append node and replace by <>(internal) node. 
            We will add (40) from right-most bit. That caused the (30) to concatenate with (40) by <B>(internal) node. 
            Move the <B> to to second left-most bit as it has 2 leafs but there also exists 2 leafs tree on the second bit.
            We need to concate <A> and <B> by <C> node and keep the binary representation as "100".
            After rebalanced the tree, the right-most 2 bits are 0s which means we can add 3 more leafs 
            before we will need to to re-balance the tree again.
             
        Scala View: val t4 = appendLeaf(t3, new Single[Int](40))
             
        Binary View:           1        1                                                  1          0         0
        Tree View:               Append[T]                                                <C>
                                 /    \                                                  /   \       empty     empty
                                /      \                                               /       \
                              <A>     (30) append (40)      transform to             /          \
                             /   \                                                 <A>          <B>
                            /     \                                               /   \        /   \
                          (10)   (20)                                            /     \      /     \
                                                                               (10)   (20)  (30)   (40) 
                                                                                 
                                                                                 
    Step 5) Add leaf (50). The binary representation is "101" for 5 leafs. 
            The previous binary bit representation which is "100" is enough for keeping 5 leafs just append (50) 
            without carrying to left most bit. So we will concatenate it by Append node. 
                    
        Scala View: val t4 = appendLeaf(t3, new Single[Int](50))
                 
        Binary View:           1          0         0                                             1   0        1
        Tree View:            <C>
                             /   \      empty      empty append (50)                                 empty
                           /       \
                         /          \                                      transform to              Append[T] 
                       <A>          <B>                                                             /         \
                      /   \        /   \                                                           /           \
                     /     \      /     \                                                        <C>          (50)
                   (10)   (20)  (30)   (40)                                                     /   \
                                                                                              /       \
                                                                                            /          \  
                                                                                          <A>          <B>
                                                                                         /   \        /   \ 
                                                                                        /     \      /     \  
                                                                                      (10)   (20)  (30)   (40)   
                     


#### ConcBuffers
From the data structure above, Now we can express it more efficient and easy to use by ConcBuffers.

```scala
object ExampleMain {

  import ConcBuffer._

  def main(args: Array[String]): Unit = {
    val xs = for (i <- 1 to 10000000) yield i

    xs.par.aggregate(new ConcBuffer[Int](100, ConcTree.Empty))(_ += _, _ combine _)
  }

}
```