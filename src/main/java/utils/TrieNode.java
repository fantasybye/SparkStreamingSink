package utils;

import java.util.HashMap;
import java.util.Map;


    /* @Author: Huaze Shen
        * @Date: 2018-06-06
        * @Description: 基于HashMap实现的Trie树节点
        */
public class TrieNode {
        // 字符
        private Character character;
        // 子节点集合
        private Map<Character, TrieNode> nodeMap;
        // state用于表示当前节点是否对应一个完整的词
        private boolean state;
        // match用于表示当前节点匹配的词index
        private int match;

        public TrieNode(Character character, Map<Character, TrieNode> nodeMap) {
                this.character = character;
                this.nodeMap = nodeMap;
        }
        /* 添加字符
        */
        public TrieNode addCharacter(Character character) {
                if (nodeMap == null) {
                        nodeMap = new HashMap<>();
                }
                TrieNode node = nodeMap.get(character);
                if (node == null) {
                        TrieNode newNode = new TrieNode(character, null);
                        nodeMap.put(character, newNode);
                        node = newNode;
                }
                return node;
        }

        public Map<Character, TrieNode> getNodeMap() {
                return this.nodeMap;
        }

        public TrieNode getCharacterNode(Character character) {
                return this.nodeMap.get(character);
        }

        public Character getCharacter() {
                return this.character;
        }

        public boolean isWord() {
                return this.state;
        }

        public void setCharacter(Character character) {
                this.character = character;
        }

        public void setNodeMap(Map<Character, TrieNode> nodeMap) {
                this.nodeMap = nodeMap;
        }

        public void setState(boolean state) {
                this.state = state;
        }

        public void setMatch(int match) {
                this.match = match;
        }

        public int getMatch(){
                return this.match;
        }
}