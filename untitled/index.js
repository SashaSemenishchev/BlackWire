function isPrime(num){
    if (num === 2) return true;
    if (num <= 1 || num % 2 === 0) return false;
    const sqrt_num = Math.sqrt(num)
    for(let div = 0; div <= sqrt_num; div += 2){
         if(num % div === 0) return false
    }
    return true
}

const N = 10000000
let start, end
start = Date.now()
for(let i = 0; i < N; i++){
    isPrime(i)
}
end = Date.now();
console.log(end - start)