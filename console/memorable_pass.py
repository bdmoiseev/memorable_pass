#!/usr/bin/python

import hashlib
import argparse
import sys
import getpass
import warnings

consonants = 'qwrtypsdfghjkzxcvbnmQWRTYPSDFGHJKLZXCVBNM'
vowels = 'aeiouAEOU'
specsymbols = ['()', ')(', '[]', '][', '{}', '}{', '!!', '??', '..', \
    ',,', '<>', '><', '/\\', '\\/', '||', '__', '@@', '::', ';;', '""', \
    '\'\'', '##', '$$', '%%', '^^', '&&', '**', '--', '++', '==']
digits = '23456789'

class CharByHashGenerator:
    def __init__(self, string):
        assert len(string) > 0
        self.hash = reduce(lambda x, y: x * 256 + y,
            map(ord, hashlib.md5(string).digest()))

    def next(self, chars):
        assert len(chars) > 0
        if self.hash < len(chars) * 1000:
            raise RuntimeError('Too many symbols required');
        next_char = chars[self.hash % len(chars)]
        self.hash /= len(chars)
        return next_char

def pass_by_string(string, n_syllables, n_end_symbols, n_outer_symbols):
    char_generator = CharByHashGenerator(string)

    result = ''
    for i in xrange(n_syllables):
        result += char_generator.next(consonants)
        result += char_generator.next(vowels)

    result += "_" if n_end_symbols > 0 else ''
    for i in xrange(n_end_symbols):
        result += char_generator.next(consonants + digits)

    for i in xrange(n_outer_symbols):
        char_pair = char_generator.next(specsymbols)
        result = char_pair[0] + result + char_pair[1]

    return result;

def run():
    argument_parser = argparse.ArgumentParser(description='Generates unique ' \
        'human-readable password by name of the service (website, app, mail ' \
        'server et.c.) and your secret password')
    argument_parser.add_argument('-p', '--pipe', action='store_true',
        help='Use in pipe mode (convert input strings to passwords)')
    argument_parser.add_argument('--n_syllables', action='store', type=int,
        default=4, help='number of syllables in main part of the password')
    argument_parser.add_argument('--n_end_symbols', action='store', type=int,
        default=2, help='number of additional symbols')
    argument_parser.add_argument('--n_outer_symbols', action='store', type=int,
        default=2, help='number of outer symbols')
    arguments = argument_parser.parse_args()

    if arguments.n_syllables <= 0:
        raise RuntimeError("Number of syllables must be positive");
    if arguments.n_end_symbols < 0:
        raise RuntimeError("Number of end symbols must be non-negative");
    if arguments.n_outer_symbols < 0:
        raise RuntimeError("Number of outer symbols must be non-negative");

    if arguments.pipe:
        while True:
            try:
                line = raw_input()
                print pass_by_string(line, arguments.n_syllables,
                    arguments.n_end_symbols, arguments.n_outer_symbols)
            except EOFError:
                break
    else:
        sys.stdout.write('Service name: ')
        service_name = raw_input()
        if service_name != service_name.strip():
            warnings.warn("Whitespaces in service name")
        if len(service_name) == 0:
            warnings.warn("Empty name of the service")

        secret = getpass.getpass('Secret password:')
        if len(secret) == 0:
            raise RuntimeError("Secret password cannot be empty")
        repeat = getpass.getpass('Repeat secret password:')
        if repeat != secret:
            raise RuntimeError('Secret passwords are not equal, try again')
        print pass_by_string(service_name + secret, arguments.n_syllables,
            arguments.n_end_symbols, arguments.n_outer_symbols)

if __name__ == '__main__':
    try:
        run()
    except Exception as e:
        print >>sys.stderr, "ERROR: %s" % e.message
        sys.exit(1)