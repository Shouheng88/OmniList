package me.shouheng.omnilist.widget.tools;

/**
 * Created by wangshouheng on 2017/3/31.*/
public interface IItemTouchHelperAdapter {

    /**
     * View type of current position, here are three options:
     * 1.Header of list;
     * 2.Footer of list;
     * 3.Normal list item.*/
    enum ViewType {
        NORMAL(0),
        HEADER(1),
        FOOTER(2);

        public final int id;

        ViewType(int id) {
            this.id = id;
        }

        public static ViewType getTypeById(int id) {
            for (ViewType type : values()){
                if (type.id == id){
                    return type;
                }
            }
            throw new IllegalArgumentException("illegal id");
        }
    }

    void onItemMoved(int fromPosition, int toPosition);

    void onItemRemoved(int position);

    void afterMoved();
}
