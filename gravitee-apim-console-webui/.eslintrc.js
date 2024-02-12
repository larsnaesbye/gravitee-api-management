module.exports = {
  env: {
    browser: true,
    node: true,
    es2020: true,
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    project: './tsconfig.json',
    sourceType: 'module',
  },
  settings: {
    'import/resolver': {
      typescript: {},
    },
  },
  ignorePatterns: ['**/*.js', '**/*.json', '**/*.html'],
  plugins: ['eslint-plugin-jsdoc', '@typescript-eslint', 'eslint-plugin-import', 'rxjs'],
  extends: [
    'angular',
    'eslint:recommended',
    'plugin:@typescript-eslint/eslint-recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:rxjs/recommended',
    'plugin:import/recommended',
    'plugin:import/typescript',
    'prettier',
    'plugin:storybook/recommended',
  ],
  reportUnusedDisableDirectives: true,
  rules: {
    'angular/no-private-call': 'error',
    'angular/controller-as-route': 'error',
    'angular/controller-name': 'error',
    'angular/module-setter': 'error',
    'angular/log': 'error',
    'angular/on-watch': 'error',
    'angular/no-service-method': 'off',
    'angular/module-getter': 'off',
    'angular/definedundefined': 'off',
    'angular/document-service': 'off',
    'angular/json-functions': 'off',
    'angular/typecheck-array': 'off',
    'angular/typecheck-string': 'off',
    'angular/typecheck-function': 'off',
    'angular/window-service': 'off',
    'angular/interval-service': 'off',
    'angular/timeout-service': 'off',
    'no-bitwise': 'warn',
    'no-redeclare': 'warn',
    'no-useless-escape': 'error',
    'no-prototype-builtins': 'warn',
    'no-cond-assign': 'warn',
    '@typescript-eslint/ban-types': 'error',
    '@typescript-eslint/no-unused-vars': [
      'error',
      {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
        caughtErrorsIgnorePattern: '^_',
      },
    ],
    '@typescript-eslint/ban-ts-comment': 'warn',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    eqeqeq: ['error', 'smart'],
    'guard-for-in': 'warn',
    'id-blacklist': 'off',
    'id-match': 'off',
    'jsdoc/check-alignment': 'error',
    'jsdoc/check-indentation': 'off',
    'jsdoc/newline-after-description': 'error',
    'no-caller': 'error',
    'no-console': [
      'warn',
      {
        allow: [
          'warn',
          'dir',
          'timeLog',
          'assert',
          'clear',
          'count',
          'countReset',
          'group',
          'groupEnd',
          'table',
          'dirxml',
          'error',
          'groupCollapsed',
          'Console',
          'profile',
          'profileEnd',
          'timeStamp',
          'context',
        ],
      },
    ],
    'no-debugger': 'error',
    'no-empty': 'off',
    'no-eval': 'error',
    'no-fallthrough': 'error',
    'no-new-wrappers': 'error',
    'no-underscore-dangle': 'off',
    'no-unused-labels': 'error',
    radix: 'error',
    'spaced-comment': [
      'warn',
      'always',
      {
        markers: ['/'],
      },
    ],
    'import/order': [
      'error',
      {
        groups: ['internal', 'external', 'builtin', 'object', 'type', 'index', 'sibling', 'parent'],
        'newlines-between': 'always',
      },
    ],
    'rxjs/no-sharereplay': ['off'],
    'rxjs/no-subject-unsubscribe': ['off'],
    'rxjs/no-implicit-any-catch': ['off'],
  },
};
