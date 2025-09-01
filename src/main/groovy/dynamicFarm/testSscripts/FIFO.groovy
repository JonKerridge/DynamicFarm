package dynamicFarm.testSscripts

List fifo=[]
fifo << 4
println "$fifo"
fifo << 7
fifo << 9
println "$fifo"
println " popped ${fifo.pop()}"
println "$fifo"
println " popped ${fifo.pop()}"
println "$fifo"
println " popped ${fifo.pop()}"
println "$fifo"
println " popped ${fifo.pop()}"  // should cause error
println "$fifo"
