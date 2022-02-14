import { keywordToGrammar } from "../src/tokens";

test("Check tokens", () => {
  expect(keywordToGrammar("if")).toEqual({
    0: {
      productions: {i: 1},
      isFinal: false
    },
    1: {
      productions: {f: 2},
      isFinal: false
    },
    2: {
      productions: {},
      isFinal: true
    }, 
  })
})
