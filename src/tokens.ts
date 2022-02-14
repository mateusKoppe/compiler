export interface Grammar {
  [state: string]: {
    productions: {
      [letter: string]: string
    },
    isFinal: boolean
  }
}

enum TOKEN_TYPE { GRAMMAR, KEYWORD }
export interface Token {
  type: TOKEN_TYPE,
  definition: String | Grammar
}

const LETTERS = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
  'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
  'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
  'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
  'Y', 'Z'];

const NUMBERS = ['0', '1', '2', '3', '4', '5', '7', '8', '9'];

export const TOKENS: { [name: string]: Token } = {
  IF: {
    type: TOKEN_TYPE.KEYWORD,
    definition: "if"
  },
  VAR: {
    type: TOKEN_TYPE.GRAMMAR,
    definition: {
      A: {
        productions: { ...LETTERS.reduce((acc, letter) => ({ ...acc, [letter]: "B" }), {}) },
        isFinal: false
      },
      B: {
        productions: { ...[...LETTERS, ...NUMBERS].reduce((acc, letter) => ({ ...acc, [letter]: "B" }), {}) },
        isFinal: true
      }
    }
  }
}

export const keywordToGrammar = (keyword: string): Grammar => {
  type reduceProp = [Grammar, number]

  const grammarReduce = ([state = {}, index = 0]: reduceProp, letter: string): reduceProp => [{ ...state, [index]: { productions: { [letter]: index + 1 }, isFinal: false } }, index + 1]
  const [grammar, lastIndex] = keyword.split("").reduce(grammarReduce, [{}, 0] as reduceProp);

  return { ...grammar, [lastIndex]: { productions: {}, isFinal: true } };
}
