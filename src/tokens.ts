export enum Tokens {
  OPEN_PARENTHESIS,
  CLOSE_PARENTHESIS,
  OPEN_BRACE,
  CLOSE_BRACE,
  SEMICOLON,
  WHITESPACE,
  PLUS,
  MINUS,
  TIMES,
  DIVIDE,
  EQUAL,
  DOUBLE_EQUAL,
  BIGGER,
  BIGGER_EQUAL,
  SMALLER,
  SMALLER_EQUAL,
  IF,
  WHILE,
  ID,
}

export interface Grammar {
  [state: string]: {
    productions: {
      [letter: string]: string;
    };
    isFinal: boolean;
  };
}

enum TOKEN_TYPE {
  GRAMMAR,
  KEYWORD,
}
export interface Token {
  type: TOKEN_TYPE;
  grammar: Grammar;
}

const LETTERS = [
  "a",
  "b",
  "c",
  "d",
  "e",
  "f",
  "g",
  "h",
  "i",
  "j",
  "k",
  "l",
  "m",
  "n",
  "o",
  "p",
  "q",
  "r",
  "s",
  "t",
  "u",
  "v",
  "w",
  "x",
  "y",
  "z",
  "A",
  "B",
  "C",
  "D",
  "E",
  "F",
  "G",
  "H",
  "I",
  "J",
  "K",
  "L",
  "M",
  "N",
  "O",
  "P",
  "Q",
  "R",
  "S",
  "T",
  "U",
  "V",
  "W",
  "X",
  "Y",
  "Z",
];

const NUMBERS = ["0", "1", "2", "3", "4", "5", "7", "8", "9"];

export const keywordToGrammar = (keyword: string): Grammar => {
  type reduceProp = [Grammar, number];

  const grammarReduce = (
    [state = {}, index = 0]: reduceProp,
    letter: string
  ): reduceProp => [
    {
      ...state,
      [index]: { productions: { [letter]: index + 1 }, isFinal: false },
    },
    index + 1,
  ];
  const [grammar, lastIndex] = keyword
    .split("")
    .reduce(grammarReduce, [{}, 0] as reduceProp);

  return { ...grammar, [lastIndex]: { productions: {}, isFinal: true } };
};

type TokenDict = { [name: string]: Grammar };

const SEPARATORS: TokenDict = {
  [Tokens.OPEN_PARENTHESIS]: keywordToGrammar("("),
  [Tokens.CLOSE_PARENTHESIS]: keywordToGrammar(")"),
  [Tokens.OPEN_BRACE]: keywordToGrammar("{"),
  [Tokens.CLOSE_BRACE]: keywordToGrammar("}"),
  [Tokens.SEMICOLON]: keywordToGrammar(";"),
  [Tokens.WHITESPACE]: keywordToGrammar(" "),
};

const OPERATORS: TokenDict = {
  [Tokens.PLUS]: keywordToGrammar("+"),
  [Tokens.MINUS]: keywordToGrammar("-"),
  [Tokens.TIMES]: keywordToGrammar("*"),
  [Tokens.DIVIDE]: keywordToGrammar("/"),
  [Tokens.EQUAL]: keywordToGrammar("="),
  [Tokens.DOUBLE_EQUAL]: keywordToGrammar("=="),
  [Tokens.BIGGER]: keywordToGrammar(">"),
  [Tokens.BIGGER_EQUAL]: keywordToGrammar(">="),
  [Tokens.SMALLER]: keywordToGrammar("<"),
  [Tokens.SMALLER_EQUAL]: keywordToGrammar("<="),
};

export const TOKENS: TokenDict = {
  [Tokens.IF]: keywordToGrammar("if"),
  [Tokens.WHILE]: keywordToGrammar("while"),
  [Tokens.ID]: {
    A: {
      productions: {
        ...LETTERS.reduce((acc, letter) => ({ ...acc, [letter]: "B" }), {}),
      },
      isFinal: false,
    },
    B: {
      productions: {
        ...[...LETTERS, ...NUMBERS].reduce(
          (acc, letter) => ({ ...acc, [letter]: "B" }),
          {}
        ),
      },
      isFinal: true,
    },
  },
  ...SEPARATORS,
  ...OPERATORS,
};

// export const mergeGrammar = (grammarA: Grammar, grammarB: Grammar): Grammar => {
//   return grammarA;
// };
