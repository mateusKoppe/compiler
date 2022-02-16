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

interface GrammarState {
  productions: {
    [char: string]: number[];
  };
  isFinal: boolean;
}

export interface Grammar {
  [state: number]: GrammarState;
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
      [index]: { productions: { [letter]: [index + 1] }, isFinal: false },
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
    0: {
      productions: {
        ...LETTERS.reduce((acc, letter) => ({ ...acc, [letter]: [1] }), {}),
      },
      isFinal: false,
    },
    1: {
      productions: {
        ...[...LETTERS, ...NUMBERS].reduce(
          (acc, letter) => ({ ...acc, [letter]: [1] }),
          {}
        ),
      },
      isFinal: true,
    },
  },
  ...SEPARATORS,
  ...OPERATORS,
};

export const mergeStates = (...states: GrammarState[]): GrammarState => {
  const letters = states.map((s) => Object.keys(s.productions)).flat();

  return {
    isFinal: states.some((a) => a.isFinal),
    productions: letters.reduce(
      (acc, index) => ({
        ...acc,
        [index]: [
          ...new Set(
            states
              .map((s) => s.productions[index])
              .filter((s) => s)
              .flat()
          ),
        ],
      }),
      {}
    ),
  };
};

export const fixGrammarConflic = (
  grammar: Grammar,
  usedStates: number[]
): Grammar => {
  const nextAvaiableState = (usedStates: number[] = [], state = 0): number =>
    usedStates.includes(state)
      ? nextAvaiableState(usedStates, state + 1)
      : state;

  const stateRemapReducer = (
    [acc, usedStates]: [{ [state: number]: number }, number[]],
    state: number
  ): [{ [state: number]: number }, number[]] => {
    const transitionState = usedStates.includes(state)
      ? nextAvaiableState(usedStates)
      : state;
    return [
      {
        ...acc,
        [state]: transitionState,
      },
      [...usedStates, transitionState],
    ];
  };

  const [remapedStates] = Object.keys(grammar)
    .map(Number)
    .reduce(stateRemapReducer, [{}, usedStates]);

  const remapState = (state: number) => remapedStates[state] ?? state;

  return Object.entries(grammar)
    .map(([key, value]) => [+key, value])
    .reduce(
      (acc: Grammar, [key, grammarState]) => ({
        ...acc,
        [remapState(key)]: {
          ...grammarState,
          productions: Object.fromEntries(
            Object.entries(grammarState.productions).map(([key, states]) => [
              key,
              (states as []).map(remapState),
            ])
          ),
        },
      }),
      {} as Grammar
    );
};

// export const mergeGrammar = (grammarA: Grammar, grammarB: Grammar): Grammar => {
//   const { 0: startA, ...statesA } = grammarA;
//   const { 0: startB, ...statesB } = grammarB;

//   const usedStates = Object.keys(grammarA);

//   return grammarA || grammarB;
// };
