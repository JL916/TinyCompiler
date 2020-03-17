### 类c语言编译器

#### 特点

- LL(1) 自顶向下预测分析法
- 支持函数调用
- java swing 界面
- 可解释运行
- 不支持函数声明语句 函数定义顺序无要求
- 表达式求值顺序自右向左 无优先级区别 可添加括号
- 不支持数组
- 不支持全局变量
- ...

#### 目录结构

src 源码  
    |- analyser 语义分析与中间代码生成  
    |- compiler 编译总控程序与界面  
    |- error 错误类  
    |- interpreter 解释程序  
    |- parser 语法分析  
    |- tokenizer 词法分析  

grammar.txt 文法规则文件 需要与程序放在同一路径下

test/test?.c 测试用例