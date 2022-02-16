import {
  fixGrammarConflic,
  Grammar,
  keywordToGrammar,
  mergeStates,
} from "../src/tokens";

const grammarIf: Grammar = {
  0: {
    productions: { i: [1] },
    isFinal: false,
  },
  1: {
    productions: { f: [2] },
    isFinal: false,
  },
  2: {
    productions: {},
    isFinal: true,
  },
};

test("keywordToGrammar", () => {
  expect(keywordToGrammar("if")).toEqual(grammarIf);
});

test("fixGrammarConflic", () => {
  expect(fixGrammarConflic(grammarIf, [1, 3])).toEqual({
    0: {
      productions: { i: [2] },
      isFinal: false,
    },
    2: {
      productions: { f: [4] },
      isFinal: false,
    },
    4: {
      productions: {},
      isFinal: true,
    },
  });
});

// test("mergeStates", () => {
//   expect(
//     mergeStates(
//       {
//         productions: { i: [1] },
//         isFinal: false,
//       },
//       {
//         productions: { f: [2], i: [2] },
//         isFinal: true,
//       }
//     )
//   ).toEqual({
//     productions: { i: [1, 2], f: [2] },
//     isFinal: true,
//   });
// });
