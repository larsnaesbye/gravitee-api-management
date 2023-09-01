import { parse } from './index';

describe('version', function () {
  describe('parse', function () {
    it.each`
      versionToParse     | versionFull | major  | minor  | patch  | qualifierFull | qualifierName | qualifierVersion
      ${'1.2.3-alpha.4'} | ${'1.2.3'}  | ${'1'} | ${'2'} | ${'3'} | ${'alpha.4'}  | ${'alpha'}    | ${'4'}
      ${'1.2.3'}         | ${'1.2.3'}  | ${'1'} | ${'2'} | ${'3'} | ${''}         | ${''}         | ${''}
    `(
      'returns parsed version $versionToParse',
      ({ versionToParse, versionFull, major, minor, patch, qualifierFull, qualifierName, qualifierVersion }) => {
        expect(parse(versionToParse)).toEqual({
          version: {
            full: versionFull,
            major,
            minor,
            patch,
          },
          qualifier: {
            full: qualifierFull,
            name: qualifierName,
            version: qualifierVersion,
          },
        });
      },
    );
  });
});
