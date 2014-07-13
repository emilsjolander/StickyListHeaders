package se.emilsjolander.stickylistheaders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * a hash map can maintain an one-to-many relationship which the value only belongs to one “one” part
 * and the map also support getKey by value quickly
 *
 * @author lsjwzh
 */
class DistinctMultiHashMap<TKey,TItemValue> {
    private IDMapper<TKey, TItemValue> mIDMapper;

    interface IDMapper<TKey,TItemValue>{
        public Object keyToKeyId(TKey key);
        public TKey keyIdToKey(Object keyId);
        public Object valueToValueId(TItemValue value);
        public TItemValue valueIdToValue(Object valueId);
    }

    LinkedHashMap<Object,List<TItemValue>> mKeyToValuesMap = new LinkedHashMap<Object, List<TItemValue>>();
    LinkedHashMap<Object,TKey> mValueToKeyIndexer = new LinkedHashMap<Object, TKey>();

    DistinctMultiHashMap(){
         this(new IDMapper<TKey, TItemValue>() {
             @Override
             public Object keyToKeyId(TKey key) {
                 return key;
             }

             @Override
             public TKey keyIdToKey(Object keyId) {
                 return (TKey) keyId;
             }

             @Override
             public Object valueToValueId(TItemValue value) {
                 return value;
             }

             @Override
             public TItemValue valueIdToValue(Object valueId) {
                 return (TItemValue) valueId;
             }
         });
    }
    DistinctMultiHashMap(IDMapper<TKey, TItemValue> idMapper){
        mIDMapper = idMapper;
    }

    public List<TItemValue> get(TKey key){
        //todo immutable
        return mKeyToValuesMap.get(mIDMapper.keyToKeyId(key));
    }
    public TKey getKey(TItemValue value){
        return mValueToKeyIndexer.get(mIDMapper.valueToValueId(value));
    }

    public void add(TKey key,TItemValue value){
        Object keyId = mIDMapper.keyToKeyId(key);
        if(mKeyToValuesMap.get(keyId)==null){
            mKeyToValuesMap.put(keyId,new ArrayList<TItemValue>());
        }
        //remove old relationship
        TKey keyForValue = getKey(value);
        if(keyForValue !=null){
            mKeyToValuesMap.get(mIDMapper.keyToKeyId(keyForValue)).remove(value);
        }
        mValueToKeyIndexer.put(mIDMapper.valueToValueId(value), key);
        if(!containsValue(mKeyToValuesMap.get(mIDMapper.keyToKeyId(key)),value)) {
            mKeyToValuesMap.get(mIDMapper.keyToKeyId(key)).add(value);
        }
    }

    public void removeKey(TKey key){
        if(mKeyToValuesMap.get(mIDMapper.keyToKeyId(key))!=null){
            for (TItemValue value : mKeyToValuesMap.get(mIDMapper.keyToKeyId(key))){
                mValueToKeyIndexer.remove(mIDMapper.valueToValueId(value));
            }
            mKeyToValuesMap.remove(mIDMapper.keyToKeyId(key));
        }
    }
    public void removeValue(TItemValue value){
        if(getKey(value)!=null){
            List<TItemValue> itemValues = mKeyToValuesMap.get(mIDMapper.keyToKeyId(getKey(value)));
            if(itemValues!=null){
                itemValues.remove(value);
            }
        }
        mValueToKeyIndexer.remove(mIDMapper.valueToValueId(value));
    }

    public void clear(){
        mValueToKeyIndexer.clear();
        mKeyToValuesMap.clear();
    }

    public void clearValues(){
        for (Map.Entry<Object,List<TItemValue>> entry:entrySet()){
            if(entry.getValue()!=null){
                entry.getValue().clear();
            }
        }
        mValueToKeyIndexer.clear();
    }

    public Set<Map.Entry<Object,List<TItemValue>>> entrySet(){
        return mKeyToValuesMap.entrySet();
    }

    public Set<Map.Entry<Object,TKey>> reverseEntrySet(){
        return mValueToKeyIndexer.entrySet();
    }

    public int size(){
        return mKeyToValuesMap.size();
    }
    public int valuesSize(){
        return mValueToKeyIndexer.size();
    }

    protected boolean containsValue(List<TItemValue> list,TItemValue  value){
        for (TItemValue itemValue :list){
            if(mIDMapper.valueToValueId(itemValue).equals(mIDMapper.valueToValueId(value))){
                return true;
            }
        }
        return false;
    }

    /**
     * @param position
     * @return
     */
    public TItemValue getValueByPosition(int position){
        Object[] vauleIdArray = mValueToKeyIndexer.keySet().toArray();
        if(position>vauleIdArray.length){
            throw new IndexOutOfBoundsException();
        }
        Object valueId = vauleIdArray[position];
        return mIDMapper.valueIdToValue(valueId);
    }
}
